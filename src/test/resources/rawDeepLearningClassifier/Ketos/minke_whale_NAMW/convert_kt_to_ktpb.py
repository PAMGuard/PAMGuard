"""
Convert a ketos model (.kt - weights checkpoint + recipe) to a PAMGuard compatible
Ketos protobuf model (.ktpb - TensorFlow SavedModel + metadata).

The .ktpb is a zip archive containing
    model/          - the TensorFlow SavedModel with a serving_default signature
    audio_repr.json - PAMGuard (jdl4pam) metadata describing the transforms
    recipe.json     - the original ketos recipe
    audio_repr_ketos.json - the original ketos/ecosound audio representation

Usage:
    python convert_kt_to_ktpb.py --model model_NAMW_20240523.kt
        --pg_json audio_repr_pg.json --out model_NAMW_20240523.ktpb

@author: Jamie Macaulay
"""

import argparse
import json
import os
import shutil
import tempfile
import zipfile

import numpy as np
import tensorflow as tf

# ketos 2.7 creates Adam optimizers with the "decay" argument which the Keras
# 2.11+ optimizers reject. The legacy optimizer is identical for inference.
tf.keras.optimizers.Adam = tf.keras.optimizers.legacy.Adam

import ketos.neural_networks


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--model", required=True)
    parser.add_argument("--pg_json", required=True, help="PAMGuard metadata json")
    parser.add_argument("--out", required=True)
    args = parser.parse_args()

    with open(args.pg_json) as f:
        pg_json = json.load(f)
    input_shape = pg_json["model_info"]["input_shape"]  # [-1, time, freq, 1]

    tmp_dir = tempfile.mkdtemp()
    model, _ = ketos.neural_networks.load_model_file(args.model, os.path.join(tmp_dir, "kt"), load_audio_repr=True)
    keras_model = model.model

    # build the model and check the ketos output against the Keras model output.
    x = np.random.RandomState(42).rand(3, input_shape[1], input_shape[2]).astype(np.float32) * 40
    ketos_out = model.run_on_batch(x, return_raw_output=True)
    keras_out = keras_model(x[..., np.newaxis], training=False).numpy()
    assert np.allclose(ketos_out, keras_out, atol=1e-5), "ketos and keras outputs differ"

    @tf.function(input_signature=[tf.TensorSpec([None, input_shape[1], input_shape[2], 1], tf.float32,
                                                 name="input_1")])
    def serve(input_1):
        return {"output_1": keras_model(input_1, training=False)}

    export_dir = os.path.join(tmp_dir, "ktpb")
    tf.saved_model.save(keras_model, os.path.join(export_dir, "model"), signatures={"serving_default": serve})

    # check the saved model gives the same answer.
    loaded = tf.saved_model.load(os.path.join(export_dir, "model"))
    saved_out = loaded.signatures["serving_default"](input_1=tf.constant(x[..., np.newaxis]))["output_1"].numpy()
    assert np.allclose(ketos_out, saved_out, atol=1e-5), "SavedModel output differs"
    print("Saved model output matches ketos:\n", saved_out)

    with open(os.path.join(export_dir, "audio_repr.json"), "w") as f:
        json.dump(pg_json, f, indent=2)
    with zipfile.ZipFile(args.model) as kt:
        with open(os.path.join(export_dir, "recipe.json"), "wb") as f:
            f.write(kt.read("recipe.json"))
        with open(os.path.join(export_dir, "audio_repr_ketos.json"), "wb") as f:
            f.write(kt.read("audio_repr.json"))

    if os.path.exists(args.out):
        os.remove(args.out)
    with zipfile.ZipFile(args.out, "w", zipfile.ZIP_DEFLATED) as zf:
        for root, _, files in os.walk(export_dir):
            for file in files:
                path = os.path.join(root, file)
                zf.write(path, os.path.relpath(path, export_dir))
    shutil.rmtree(tmp_dir)
    print("Written " + args.out)


if __name__ == "__main__":
    main()
