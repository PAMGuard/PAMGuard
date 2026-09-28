package soundtrapSensor.plot;

import dataPlotsFX.data.TDDataInfoFX;
import dataPlotsFX.data.TDDataProviderFX;
import dataPlotsFX.layout.TDGraphFX;
import soundtrapSensor.SudSensorControl;
import soundtrapSensor.SudSensorDataBlock;

/**
 * FX data provider for the SudSensor module. Registered with
 * {@link dataPlotsFX.data.TDDataProviderRegisterFX} so that the heading, pitch
 * and roll lines appear as an available overlay on any TD graph.
 *
 * @author Jamie Macaulay
 */
public class SudSensorPlotProviderFX extends TDDataProviderFX {

    private final SudSensorControl sensorControl;

    public SudSensorPlotProviderFX(SudSensorControl sensorControl, SudSensorDataBlock dataBlock) {
        super(dataBlock);
        this.sensorControl = sensorControl;
    }

    @Override
    public TDDataInfoFX createDataInfo(TDGraphFX tdGraph) {
        return new SudSensorPlotInfoFX(sensorControl, this, tdGraph,
                (SudSensorDataBlock) getDataBlock());
    }
    
 
}
