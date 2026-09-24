"""
Fix the monk seal TorchScript model. The MATLAB yolov4 network uses Mish activations
(vision.cnn.mish, exported to ONNX as a placeholder) but in model_jit.pt these were
replaced by LeakyReLU(0.1). This rewrites the TorchScript forward graph so that every
call to a mish_* sub module computes x*tanh(softplus(x)) instead.
"""
import sys, torch

src, dst = sys.argv[1], sys.argv[2]
m = torch.jit.load(src, map_location='cpu').eval()
g = m.graph

n_fixed = 0
for node in list(g.nodes()):
    if node.kind() != 'prim::CallMethod':
        continue
    selfv = node.inputsAt(0).node()
    if selfv.kind() != 'prim::GetAttr' or not selfv.s('name').startswith('mish_'):
        continue
    x = node.inputsAt(1)
    with g.insert_point_guard(node):
        # softplus as in MATLAB: max(x,0) + log(1+exp(-|x|))
        zero = g.insertConstant(0.0)
        relu = g.insert('aten::relu', [x])
        absx = g.insert('aten::abs', [x])
        neg = g.insert('aten::neg', [absx])
        ex = g.insert('aten::exp', [neg])
        lp = g.insert('aten::log1p', [ex])
        sp = g.insert('aten::add', [relu, lp])
        th = g.insert('aten::tanh', [sp])
        y = g.insert('aten::mul', [x, th])
    node.output().replaceAllUsesWith(y)
    node.destroy()
    n_fixed += 1

# remove now unused GetAttr nodes
for node in list(g.nodes()):
    if node.kind() == 'prim::GetAttr' and node.s('name').startswith('mish_') and not node.output().uses():
        node.destroy()

torch._C._jit_pass_lint(g)
print('Replaced', n_fixed, 'mish calls')
torch.jit.save(m, dst)
