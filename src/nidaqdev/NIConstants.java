package nidaqdev;

/**
 * Constants from NIDAQmx.h
 * <p>
 * The original C header file had many repeats of each constant. These
 * have been commented leaving only the first instance of each declaration.
 * <p>
 * All comments are as written by NI. 
 * 
 * @author Doug Gillespie
 *
 */
public class NIConstants {

	/******************************************************************************
	 *** NI-DAQmx Attributes ******************************************************
	 ******************************************************************************/

	//********** Buffer Attributes **********
	static public final int DAQmx_Buf_Input_BufSize  = 0x186C; // Specifies the number of samples the input buffer can hold for each channel in the task. Zero indicates to allocate no buffer. Use a buffer size of 0 to perform a hardware-timed operation without using a buffer. Setting this property overrides the automatic input buffer allocation that NI-DAQmx performs.
	static public final int DAQmx_Buf_Input_OnbrdBufSize  = 0x230A; // Indicates in samples per channel the size of the onboard input buffer of the device.
	static public final int DAQmx_Buf_Output_BufSize  = 0x186D; // Specifies the number of samples the output buffer can hold for each channel in the task. Zero indicates to allocate no buffer. Use a buffer size of 0 to perform a hardware-timed operation without using a buffer. Setting this property overrides the automatic output buffer allocation that NI-DAQmx performs.
	static public final int DAQmx_Buf_Output_OnbrdBufSize  = 0x230B; // Specifies in samples per channel the size of the onboard output buffer of the device.

	//********** Calibration Info Attributes **********
	static public final int DAQmx_SelfCal_Supported  = 0x1860; // Indicates whether the device supports self calibration.
	static public final int DAQmx_SelfCal_LastTemp  = 0x1864; // Indicates in degrees Celsius the temperature of the device at the time of the last self calibration. Compare this temperature to the current onboard temperature to determine if you should perform another calibration.
	static public final int DAQmx_ExtCal_RecommendedInterval  = 0x1868; // Indicates in months the National Instruments recommended interval between each external calibration of the device.
	static public final int DAQmx_ExtCal_LastTemp  = 0x1867; // Indicates in degrees Celsius the temperature of the device at the time of the last external calibration. Compare this temperature to the current onboard temperature to determine if you should perform another calibration.
	static public final int DAQmx_Cal_UserDefinedInfo  = 0x1861; // Specifies a string that contains arbitrary, user-defined information. This number of characters in this string can be no more than Max Size.
	static public final int DAQmx_Cal_UserDefinedInfo_MaxSize  = 0x191C; // Indicates the maximum length in characters of Information.
	static public final int DAQmx_Cal_DevTemp  = 0x223B; // Indicates in degrees Celsius the current temperature of the device.

	//********** Channel Attributes **********
	static public final int DAQmx_AI_Max  = 0x17DD; // Specifies the maximum value you expect to measure. This value is in the units you specify with a units property. When you query this property, it returns the coerced maximum value that the device can measure with the current settings.
	static public final int DAQmx_AI_Min  = 0x17DE; // Specifies the minimum value you expect to measure. This value is in the units you specify with a units property. When you query this property, it returns the coerced minimum value that the device can measure with the current settings.
	static public final int DAQmx_AI_CustomScaleName  = 0x17E0; // Specifies the name of a custom scale for the channel.
	static public final int DAQmx_AI_MeasType  = 0x0695; // Indicates the measurement to take with the analog input channel and in some cases, such as for temperature measurements, the sensor to use.
	static public final int DAQmx_AI_Voltage_Units  = 0x1094; // Specifies the units to use to return voltage measurements from the channel.
	static public final int DAQmx_AI_Voltage_dBRef  = 0x29B0; // Specifies the decibel reference level in the units of the channel. When you read samples as a waveform, the decibel reference level is included in the waveform attributes.
	static public final int DAQmx_AI_Voltage_ACRMS_Units  = 0x17E2; // Specifies the units to use to return voltage RMS measurements from the channel.
	static public final int DAQmx_AI_Temp_Units  = 0x1033; // Specifies the units to use to return temperature measurements from the channel.
	static public final int DAQmx_AI_Thrmcpl_Type  = 0x1050; // Specifies the type of thermocouple connected to the channel. Thermocouple types differ in composition and measurement range.
	static public final int DAQmx_AI_Thrmcpl_ScaleType  = 0x29D0; // Specifies the method or equation form that the thermocouple scale uses.
	static public final int DAQmx_AI_Thrmcpl_CJCSrc  = 0x1035; // Indicates the source of cold-junction compensation.
	static public final int DAQmx_AI_Thrmcpl_CJCVal  = 0x1036; // Specifies the temperature of the cold junction if CJC Source is DAQmx_Val_ConstVal. Specify this value in the units of the measurement.
	static public final int DAQmx_AI_Thrmcpl_CJCChan  = 0x1034; // Indicates the channel that acquires the temperature of the cold junction if CJC Source is DAQmx_Val_Chan. If the channel is a temperature channel, NI-DAQmx acquires the temperature in the correct units. Other channel types, such as a resistance channel with a custom sensor, must use a custom scale to scale values to degrees Celsius.
	static public final int DAQmx_AI_RTD_Type  = 0x1032; // Specifies the type of RTD connected to the channel.
	static public final int DAQmx_AI_RTD_R0  = 0x1030; // Specifies in ohms the sensor resistance at 0 deg C. The Callendar-Van Dusen equation requires this value. Refer to the sensor documentation to determine this value.
	static public final int DAQmx_AI_RTD_A  = 0x1010; // Specifies the 'A' constant of the Callendar-Van Dusen equation. NI-DAQmx requires this value when you use a custom RTD.
	static public final int DAQmx_AI_RTD_B  = 0x1011; // Specifies the 'B' constant of the Callendar-Van Dusen equation. NI-DAQmx requires this value when you use a custom RTD.
	static public final int DAQmx_AI_RTD_C  = 0x1013; // Specifies the 'C' constant of the Callendar-Van Dusen equation. NI-DAQmx requires this value when you use a custom RTD.
	static public final int DAQmx_AI_Thrmstr_A  = 0x18C9; // Specifies the 'A' constant of the Steinhart-Hart thermistor equation.
	static public final int DAQmx_AI_Thrmstr_B  = 0x18CB; // Specifies the 'B' constant of the Steinhart-Hart thermistor equation.
	static public final int DAQmx_AI_Thrmstr_C  = 0x18CA; // Specifies the 'C' constant of the Steinhart-Hart thermistor equation.
	static public final int DAQmx_AI_Thrmstr_R1  = 0x1061; // Specifies in ohms the value of the reference resistor if you use voltage excitation. NI-DAQmx ignores this value for current excitation.
	static public final int DAQmx_AI_ForceReadFromChan  = 0x18F8; // Specifies whether to read from the channel if it is a cold-junction compensation channel. By default, an NI-DAQmx Read function does not return data from cold-junction compensation channels. Setting this property to TRUE forces read operations to return the cold-junction compensation channel data with the other channels in the task.
	static public final int DAQmx_AI_Current_Units  = 0x0701; // Specifies the units to use to return current measurements from the channel.
	static public final int DAQmx_AI_Current_ACRMS_Units  = 0x17E3; // Specifies the units to use to return current RMS measurements from the channel.
	static public final int DAQmx_AI_Strain_Units  = 0x0981; // Specifies the units to use to return strain measurements from the channel.
	static public final int DAQmx_AI_StrainGage_GageFactor  = 0x0994; // Specifies the sensitivity of the strain gage. Gage factor relates the change in electrical resistance to the change in strain. Refer to the sensor documentation for this value.
	static public final int DAQmx_AI_StrainGage_PoissonRatio  = 0x0998; // Specifies the ratio of lateral strain to axial strain in the material you are measuring.
	static public final int DAQmx_AI_StrainGage_Cfg  = 0x0982; // Specifies the bridge configuration of the strain gages.
	static public final int DAQmx_AI_Resistance_Units  = 0x0955; // Specifies the units to use to return resistance measurements.
	static public final int DAQmx_AI_Freq_Units  = 0x0806; // Specifies the units to use to return frequency measurements from the channel.
	static public final int DAQmx_AI_Freq_ThreshVoltage  = 0x0815; // Specifies the voltage level at which to recognize waveform repetitions. You should select a voltage level that occurs only once within the entire period of a waveform. You also can select a voltage that occurs only once while the voltage rises or falls.
	static public final int DAQmx_AI_Freq_Hyst  = 0x0814; // Specifies in volts a window below Threshold Level. The input voltage must pass below Threshold Level minus this value before NI-DAQmx recognizes a waveform repetition at Threshold Level. Hysteresis can improve the measurement accuracy when the signal contains noise or jitter.
	static public final int DAQmx_AI_LVDT_Units  = 0x0910; // Specifies the units to use to return linear position measurements from the channel.
	static public final int DAQmx_AI_LVDT_Sensitivity  = 0x0939; // Specifies the sensitivity of the LVDT. This value is in the units you specify with Sensitivity Units. Refer to the sensor documentation to determine this value.
	static public final int DAQmx_AI_LVDT_SensitivityUnits  = 0x219A; // Specifies the units of Sensitivity.
	static public final int DAQmx_AI_RVDT_Units  = 0x0877; // Specifies the units to use to return angular position measurements from the channel.
	static public final int DAQmx_AI_RVDT_Sensitivity  = 0x0903; // Specifies the sensitivity of the RVDT. This value is in the units you specify with Sensitivity Units. Refer to the sensor documentation to determine this value.
	static public final int DAQmx_AI_RVDT_SensitivityUnits  = 0x219B; // Specifies the units of Sensitivity.
	static public final int DAQmx_AI_SoundPressure_MaxSoundPressureLvl  = 0x223A; // Specifies the maximum instantaneous sound pressure level you expect to measure. This value is in decibels, referenced to 20 micropascals. NI-DAQmx uses the maximum sound pressure level to calculate values in pascals for Maximum Value and Minimum Value for the channel.
	static public final int DAQmx_AI_SoundPressure_Units  = 0x1528; // Specifies the units to use to return sound pressure measurements from the channel.
	static public final int DAQmx_AI_SoundPressure_dBRef  = 0x29B1; // Specifies the decibel reference level in the units of the channel. When you read samples as a waveform, the decibel reference level is included in the waveform attributes. NI-DAQmx also uses the decibel reference level when converting Maximum Sound Pressure Level to a voltage level.
	static public final int DAQmx_AI_Microphone_Sensitivity  = 0x1536; // Specifies the sensitivity of the microphone. This value is in mV/Pa. Refer to the sensor documentation to determine this value.
	static public final int DAQmx_AI_Accel_Units  = 0x0673; // Specifies the units to use to return acceleration measurements from the channel.
	static public final int DAQmx_AI_Accel_dBRef  = 0x29B2; // Specifies the decibel reference level in the units of the channel. When you read samples as a waveform, the decibel reference level is included in the waveform attributes.
	static public final int DAQmx_AI_Accel_Sensitivity  = 0x0692; // Specifies the sensitivity of the accelerometer. This value is in the units you specify with Sensitivity Units. Refer to the sensor documentation to determine this value.
	static public final int DAQmx_AI_Accel_SensitivityUnits  = 0x219C; // Specifies the units of Sensitivity.
	static public final int DAQmx_AI_Is_TEDS  = 0x2983; // Indicates if the virtual channel was initialized using a TEDS bitstream from the corresponding physical channel.
	static public final int DAQmx_AI_TEDS_Units  = 0x21E0; // Indicates the units defined by TEDS information associated with the channel.
	static public final int DAQmx_AI_Coupling  = 0x0064; // Specifies the coupling for the channel.
	static public final int DAQmx_AI_Impedance  = 0x0062; // Specifies the input impedance of the channel.
	static public final int DAQmx_AI_TermCfg  = 0x1097; // Specifies the terminal configuration for the channel.
	static public final int DAQmx_AI_InputSrc  = 0x2198; // Specifies the source of the channel. You can use the signal from the I/O connector or one of several calibration signals. Certain devices have a single calibration signal bus. For these devices, you must specify the same calibration signal for all channels you connect to a calibration signal.
	static public final int DAQmx_AI_ResistanceCfg  = 0x1881; // Specifies the resistance configuration for the channel. NI-DAQmx uses this value for any resistance-based measurements, including temperature measurement using a thermistor or RTD.
	static public final int DAQmx_AI_LeadWireResistance  = 0x17EE; // Specifies in ohms the resistance of the wires that lead to the sensor.
	static public final int DAQmx_AI_Bridge_Cfg  = 0x0087; // Specifies the type of Wheatstone bridge that the sensor is.
	static public final int DAQmx_AI_Bridge_NomResistance  = 0x17EC; // Specifies in ohms the resistance across each arm of the bridge in an unloaded position.
	static public final int DAQmx_AI_Bridge_InitialVoltage  = 0x17ED; // Specifies in volts the output voltage of the bridge in the unloaded condition. NI-DAQmx subtracts this value from any measurements before applying scaling equations.
	static public final int DAQmx_AI_Bridge_ShuntCal_Enable  = 0x0094; // Specifies whether to enable a shunt calibration switch. Use Shunt Cal Select to select the switch(es) to enable.
	static public final int DAQmx_AI_Bridge_ShuntCal_Select  = 0x21D5; // Specifies which shunt calibration switch(es) to enable. Use Shunt Cal Enable to enable the switch(es) you specify with this property.
	static public final int DAQmx_AI_Bridge_ShuntCal_GainAdjust  = 0x193F; // Specifies the result of a shunt calibration. NI-DAQmx multiplies data read from the channel by the value of this property. This value should be close to 1.0.
	static public final int DAQmx_AI_Bridge_Balance_CoarsePot  = 0x17F1; // Specifies by how much to compensate for offset in the signal. This value can be between 0 and 127.
	static public final int DAQmx_AI_Bridge_Balance_FinePot  = 0x18F4; // Specifies by how much to compensate for offset in the signal. This value can be between 0 and 4095.
	static public final int DAQmx_AI_CurrentShunt_Loc  = 0x17F2; // Specifies the shunt resistor location for current measurements.
	static public final int DAQmx_AI_CurrentShunt_Resistance  = 0x17F3; // Specifies in ohms the external shunt resistance for current measurements.
	static public final int DAQmx_AI_Excit_Src  = 0x17F4; // Specifies the source of excitation.
	static public final int DAQmx_AI_Excit_Val  = 0x17F5; // Specifies the amount of excitation that the sensor requires. If Voltage or Current is DAQmx_Val_Voltage, this value is in volts. If Voltage or Current is DAQmx_Val_Current, this value is in amperes.
	static public final int DAQmx_AI_Excit_UseForScaling  = 0x17FC; // Specifies if NI-DAQmx divides the measurement by the excitation. You should typically set this property to TRUE for ratiometric transducers. If you set this property to TRUE, set Maximum Value and Minimum Value to reflect the scaling.
	static public final int DAQmx_AI_Excit_UseMultiplexed  = 0x2180; // Specifies if the SCXI-1122 multiplexes the excitation to the upper half of the channels as it advances through the scan list.
	static public final int DAQmx_AI_Excit_ActualVal  = 0x1883; // Specifies the actual amount of excitation supplied by an internal excitation source. If you read an internal excitation source more precisely with an external device, set this property to the value you read. NI-DAQmx ignores this value for external excitation. When performing shunt calibration, some devices set this property automatically.
	static public final int DAQmx_AI_Excit_DCorAC  = 0x17FB; // Specifies if the excitation supply is DC or AC.
	static public final int DAQmx_AI_Excit_VoltageOrCurrent  = 0x17F6; // Specifies if the channel uses current or voltage excitation.
	static public final int DAQmx_AI_ACExcit_Freq  = 0x0101; // Specifies the AC excitation frequency in Hertz.
	static public final int DAQmx_AI_ACExcit_SyncEnable  = 0x0102; // Specifies whether to synchronize the AC excitation source of the channel to that of another channel. Synchronize the excitation sources of multiple channels to use multichannel sensors. Set this property to FALSE for the master channel and to TRUE for the slave channels.
	static public final int DAQmx_AI_ACExcit_WireMode  = 0x18CD; // Specifies the number of leads on the LVDT or RVDT. Some sensors require you to tie leads together to create a four- or five- wire sensor. Refer to the sensor documentation for more information.
	static public final int DAQmx_AI_Atten  = 0x1801; // Specifies the amount of attenuation to use.
	static public final int DAQmx_AI_ProbeAtten  = 0x2A88; // Specifies the amount of attenuation provided by the probe connected to the channel. Specify this attenuation as a ratio.
	static public final int DAQmx_AI_Lowpass_Enable  = 0x1802; // Specifies whether to enable the lowpass filter of the channel.
	static public final int DAQmx_AI_Lowpass_CutoffFreq  = 0x1803; // Specifies the frequency in Hertz that corresponds to the -3dB cutoff of the filter.
	static public final int DAQmx_AI_Lowpass_SwitchCap_ClkSrc  = 0x1884; // Specifies the source of the filter clock. If you need a higher resolution for the filter, you can supply an external clock to increase the resolution. Refer to the SCXI-1141/1142/1143 User Manual for more information.
	static public final int DAQmx_AI_Lowpass_SwitchCap_ExtClkFreq  = 0x1885; // Specifies the frequency of the external clock when you set Clock Source to DAQmx_Val_External. NI-DAQmx uses this frequency to set the pre- and post- filters on the SCXI-1141, SCXI-1142, and SCXI-1143. On those devices, NI-DAQmx determines the filter cutoff by using the equation f/(100*n), where f is the external frequency, and n is the external clock divisor. Refer to the SCXI-1141/1142/1143 User Manual for more...
	static public final int DAQmx_AI_Lowpass_SwitchCap_ExtClkDiv  = 0x1886; // Specifies the divisor for the external clock when you set Clock Source to DAQmx_Val_External. On the SCXI-1141, SCXI-1142, and SCXI-1143, NI-DAQmx determines the filter cutoff by using the equation f/(100*n), where f is the external frequency, and n is the external clock divisor. Refer to the SCXI-1141/1142/1143 User Manual for more information.
	static public final int DAQmx_AI_Lowpass_SwitchCap_OutClkDiv  = 0x1887; // Specifies the divisor for the output clock. NI-DAQmx uses the cutoff frequency to determine the output clock frequency. Refer to the SCXI-1141/1142/1143 User Manual for more information.
	static public final int DAQmx_AI_ResolutionUnits  = 0x1764; // Indicates the units of Resolution Value.
	static public final int DAQmx_AI_Resolution  = 0x1765; // Indicates the resolution of the analog-to-digital converter of the channel. This value is in the units you specify with Resolution Units.
	static public final int DAQmx_AI_RawSampSize  = 0x22DA; // Indicates in bits the size of a raw sample from the device.
	static public final int DAQmx_AI_RawSampJustification  = 0x0050; // Indicates the justification of a raw sample from the device.
	static public final int DAQmx_AI_ADCTimingMode  = 0x29F9; // Specifies the ADC timing mode, controlling the tradeoff between speed and effective resolution. Some ADC timing modes provide increased powerline noise rejection. On devices that have an AI Convert clock, this setting affects both the maximum and default values for Rate. You must use the same ADC timing mode for all channels on a device, but you can use different ADC timing modes for different device in the same t...
	static public final int DAQmx_AI_Dither_Enable  = 0x0068; // Specifies whether to enable dithering. Dithering adds Gaussian noise to the input signal. You can use dithering to achieve higher resolution measurements by over sampling the input signal and averaging the results.
	static public final int DAQmx_AI_ChanCal_HasValidCalInfo  = 0x2297; // Indicates if the channel has calibration information.
	static public final int DAQmx_AI_ChanCal_EnableCal  = 0x2298; // Specifies whether to enable the channel calibration associated with the channel.
	static public final int DAQmx_AI_ChanCal_ApplyCalIfExp  = 0x2299; // Specifies whether to apply the channel calibration to the channel after the expiration date has passed.
	static public final int DAQmx_AI_ChanCal_ScaleType  = 0x229C; // Specifies the method or equation form that the calibration scale uses.
	static public final int DAQmx_AI_ChanCal_Table_PreScaledVals  = 0x229D; // Specifies the reference values collected when calibrating the channel.
	static public final int DAQmx_AI_ChanCal_Table_ScaledVals  = 0x229E; // Specifies the acquired values collected when calibrating the channel.
	static public final int DAQmx_AI_ChanCal_Poly_ForwardCoeff  = 0x229F; // Specifies the forward polynomial values used for calibrating the channel.
	static public final int DAQmx_AI_ChanCal_Poly_ReverseCoeff  = 0x22A0; // Specifies the reverse polynomial values used for calibrating the channel.
	static public final int DAQmx_AI_ChanCal_OperatorName  = 0x22A3; // Specifies the name of the operator who performed the channel calibration.
	static public final int DAQmx_AI_ChanCal_Desc  = 0x22A4; // Specifies the description entered for the calibration of the channel.
	static public final int DAQmx_AI_ChanCal_Verif_RefVals  = 0x22A1; // Specifies the reference values collected when verifying the calibration. NI-DAQmx stores these values as a record of calibration accuracy and does not use them in the scaling process.
	static public final int DAQmx_AI_ChanCal_Verif_AcqVals  = 0x22A2; // Specifies the acquired values collected when verifying the calibration. NI-DAQmx stores these values as a record of calibration accuracy and does not use them in the scaling process.
	static public final int DAQmx_AI_Rng_High  = 0x1815; // Specifies the upper limit of the input range of the device. This value is in the native units of the device. On E Series devices, for example, the native units is volts.
	static public final int DAQmx_AI_Rng_Low  = 0x1816; // Specifies the lower limit of the input range of the device. This value is in the native units of the device. On E Series devices, for example, the native units is volts.
	static public final int DAQmx_AI_DCOffset  = 0x2A89; // Specifies the DC value to add to the input range of the device. Use High and Low to specify the input range. This offset is in the native units of the device .
	static public final int DAQmx_AI_Gain  = 0x1818; // Specifies a gain factor to apply to the channel.
	static public final int DAQmx_AI_SampAndHold_Enable  = 0x181A; // Specifies whether to enable the sample and hold circuitry of the device. When you disable sample and hold circuitry, a small voltage offset might be introduced into the signal. You can eliminate this offset by using Auto Zero Mode to perform an auto zero on the channel.
	static public final int DAQmx_AI_AutoZeroMode  = 0x1760; // Specifies how often to measure ground. NI-DAQmx subtracts the measured ground voltage from every sample.
	static public final int DAQmx_AI_DataXferMech  = 0x1821; // Specifies the data transfer mode for the device.
	static public final int DAQmx_AI_DataXferReqCond  = 0x188B; // Specifies under what condition to transfer data from the onboard memory of the device to the buffer.
	static public final int DAQmx_AI_DataXferCustomThreshold  = 0x230C; // Specifies the number of samples that must be in the FIFO to transfer data from the device if Data Transfer Request Condition is DAQmx_Val_OnbrdMemCustomThreshold.
	static public final int DAQmx_AI_UsbXferReqSize  = 0x2A8E; // Specifies the maximum size of a USB transfer request in bytes. Modify this value to affect performance under different combinations of operating system and device.
	static public final int DAQmx_AI_MemMapEnable  = 0x188C; // Specifies for NI-DAQmx to map hardware registers to the memory space of the application, if possible. Normally, NI-DAQmx maps hardware registers to memory accessible only to the kernel. Mapping the registers to the memory space of the application increases performance. However, if the application accesses the memory space mapped to the registers, it can adversely affect the operation of the device and possibly res...
	static public final int DAQmx_AI_RawDataCompressionType  = 0x22D8; // Specifies the type of compression to apply to raw samples returned from the device.
	static public final int DAQmx_AI_LossyLSBRemoval_CompressedSampSize  = 0x22D9; // Specifies the number of bits to return in a raw sample when Raw Data Compression Type is set to DAQmx_Val_LossyLSBRemoval.
	static public final int DAQmx_AI_DevScalingCoeff  = 0x1930; // Indicates the coefficients of a polynomial equation that NI-DAQmx uses to scale values from the native format of the device to volts. Each element of the array corresponds to a term of the equation. For example, if index two of the array is 4, the third term of the equation is 4x^2. Scaling coefficients do not account for any custom scales or sensors contained by the channel.
	static public final int DAQmx_AI_EnhancedAliasRejectionEnable  = 0x2294; // Specifies whether to enable enhanced alias rejection. By default, enhanced alias rejection is enabled on supported devices. Leave this property set to the default value for most applications.
	static public final int DAQmx_AO_Max  = 0x1186; // Specifies the maximum value you expect to generate. The value is in the units you specify with a units property. If you try to write a value larger than the maximum value, NI-DAQmx generates an error. NI-DAQmx might coerce this value to a smaller value if other task settings restrict the device from generating the desired maximum.
	static public final int DAQmx_AO_Min  = 0x1187; // Specifies the minimum value you expect to generate. The value is in the units you specify with a units property. If you try to write a value smaller than the minimum value, NI-DAQmx generates an error. NI-DAQmx might coerce this value to a larger value if other task settings restrict the device from generating the desired minimum.
	static public final int DAQmx_AO_CustomScaleName  = 0x1188; // Specifies the name of a custom scale for the channel.
	static public final int DAQmx_AO_OutputType  = 0x1108; // Indicates whether the channel generates voltage, current, or a waveform.
	static public final int DAQmx_AO_Voltage_Units  = 0x1184; // Specifies in what units to generate voltage on the channel. Write data to the channel in the units you select.
	static public final int DAQmx_AO_Voltage_CurrentLimit  = 0x2A1D; // Specifies the current limit, in amperes, for the voltage channel.
	static public final int DAQmx_AO_Current_Units  = 0x1109; // Specifies in what units to generate current on the channel. Write data to the channel in the units you select.
	static public final int DAQmx_AO_FuncGen_Type  = 0x2A18; // Specifies the kind of the waveform to generate.
	static public final int DAQmx_AO_FuncGen_Freq  = 0x2A19; // Specifies the frequency of the waveform to generate in hertz.
	static public final int DAQmx_AO_FuncGen_Amplitude  = 0x2A1A; // Specifies the zero-to-peak amplitude of the waveform to generate in volts. Zero and negative values are valid.
	static public final int DAQmx_AO_FuncGen_Offset  = 0x2A1B; // Specifies the voltage offset of the waveform to generate.
	static public final int DAQmx_AO_FuncGen_Square_DutyCycle  = 0x2A1C; // Specifies the square wave duty cycle of the waveform to generate.
	static public final int DAQmx_AO_FuncGen_ModulationType  = 0x2A22; // Specifies if the device generates a modulated version of the waveform using the original waveform as a carrier and input from an external terminal as the signal.
	static public final int DAQmx_AO_FuncGen_FMDeviation  = 0x2A23; // Specifies the FM deviation in hertz per volt when Type is DAQmx_Val_FM.
	static public final int DAQmx_AO_OutputImpedance  = 0x1490; // Specifies in ohms the impedance of the analog output stage of the device.
	static public final int DAQmx_AO_LoadImpedance  = 0x0121; // Specifies in ohms the load impedance connected to the analog output channel.
	static public final int DAQmx_AO_IdleOutputBehavior  = 0x2240; // Specifies the state of the channel when no generation is in progress.
	static public final int DAQmx_AO_TermCfg  = 0x188E; // Specifies the terminal configuration of the channel.
	static public final int DAQmx_AO_ResolutionUnits  = 0x182B; // Specifies the units of Resolution Value.
	static public final int DAQmx_AO_Resolution  = 0x182C; // Indicates the resolution of the digital-to-analog converter of the channel. This value is in the units you specify with Resolution Units.
	static public final int DAQmx_AO_DAC_Rng_High  = 0x182E; // Specifies the upper limit of the output range of the device. This value is in the native units of the device. On E Series devices, for example, the native units is volts.
	static public final int DAQmx_AO_DAC_Rng_Low  = 0x182D; // Specifies the lower limit of the output range of the device. This value is in the native units of the device. On E Series devices, for example, the native units is volts.
	static public final int DAQmx_AO_DAC_Ref_ConnToGnd  = 0x0130; // Specifies whether to ground the internal DAC reference. Grounding the internal DAC reference has the effect of grounding all analog output channels and stopping waveform generation across all analog output channels regardless of whether the channels belong to the current task. You can ground the internal DAC reference only when Source is DAQmx_Val_Internal and Allow Connecting DAC Reference to Ground at Runtime is...
	static public final int DAQmx_AO_DAC_Ref_AllowConnToGnd  = 0x1830; // Specifies whether to allow grounding the internal DAC reference at run time. You must set this property to TRUE and set Source to DAQmx_Val_Internal before you can set Connect DAC Reference to Ground to TRUE.
	static public final int DAQmx_AO_DAC_Ref_Src  = 0x0132; // Specifies the source of the DAC reference voltage. The value of this voltage source determines the full-scale value of the DAC.
	static public final int DAQmx_AO_DAC_Ref_ExtSrc  = 0x2252; // Specifies the source of the DAC reference voltage if Source is DAQmx_Val_External. The valid sources for this signal vary by device.
	static public final int DAQmx_AO_DAC_Ref_Val  = 0x1832; // Specifies in volts the value of the DAC reference voltage. This voltage determines the full-scale range of the DAC. Smaller reference voltages result in smaller ranges, but increased resolution.
	static public final int DAQmx_AO_DAC_Offset_Src  = 0x2253; // Specifies the source of the DAC offset voltage. The value of this voltage source determines the full-scale value of the DAC.
	static public final int DAQmx_AO_DAC_Offset_ExtSrc  = 0x2254; // Specifies the source of the DAC offset voltage if Source is DAQmx_Val_External. The valid sources for this signal vary by device.
	static public final int DAQmx_AO_DAC_Offset_Val  = 0x2255; // Specifies in volts the value of the DAC offset voltage. To achieve best accuracy, the DAC offset value should be hand calibrated.
	static public final int DAQmx_AO_ReglitchEnable  = 0x0133; // Specifies whether to enable reglitching. The output of a DAC normally glitches whenever the DAC is updated with a new value. The amount of glitching differs from code to code and is generally largest at major code transitions. Reglitching generates uniform glitch energy at each code transition and provides for more uniform glitches. Uniform glitch energy makes it easier to filter out the noise introduced from g...
	static public final int DAQmx_AO_Gain  = 0x0118; // Specifies in decibels the gain factor to apply to the channel.
	static public final int DAQmx_AO_UseOnlyOnBrdMem  = 0x183A; // Specifies whether to write samples directly to the onboard memory of the device, bypassing the memory buffer. Generally, you cannot update onboard memory directly after you start the task. Onboard memory includes data FIFOs.
	static public final int DAQmx_AO_DataXferMech  = 0x0134; // Specifies the data transfer mode for the device.
	static public final int DAQmx_AO_DataXferReqCond  = 0x183C; // Specifies under what condition to transfer data from the buffer to the onboard memory of the device.
	static public final int DAQmx_AO_UsbXferReqSize  = 0x2A8F; // Specifies the maximum size of a USB transfer request in bytes. Modify this value to affect performance under different combinations of operating system and device.
	static public final int DAQmx_AO_MemMapEnable  = 0x188F; // Specifies for NI-DAQmx to map hardware registers to the memory space of the application, if possible. Normally, NI-DAQmx maps hardware registers to memory accessible only to the kernel. Mapping the registers to the memory space of the application increases performance. However, if the application accesses the memory space mapped to the registers, it can adversely affect the operation of the device and possibly res...
	static public final int DAQmx_AO_DevScalingCoeff  = 0x1931; // Indicates the coefficients of a linear equation that NI-DAQmx uses to scale values from a voltage to the native format of the device. Each element of the array corresponds to a term of the equation. For example, if index two of the array is 4, the third term of the equation is 4x^2. Scaling coefficients do not account for any custom scales that may be applied to the channel.
	static public final int DAQmx_AO_EnhancedImageRejectionEnable  = 0x2241; // Specifies whether to enable the DAC interpolation filter. Disable the interpolation filter to improve DAC signal-to-noise ratio at the expense of degraded image rejection.
	static public final int DAQmx_DI_InvertLines  = 0x0793; // Specifies whether to invert the lines in the channel. If you set this property to TRUE, the lines are at high logic when off and at low logic when on.
	static public final int DAQmx_DI_NumLines  = 0x2178; // Indicates the number of digital lines in the channel.
	static public final int DAQmx_DI_DigFltr_Enable  = 0x21D6; // Specifies whether to enable the digital filter for the line(s) or port(s). You can enable the filter on a line-by-line basis. You do not have to enable the filter for all lines in a channel.
	static public final int DAQmx_DI_DigFltr_MinPulseWidth  = 0x21D7; // Specifies in seconds the minimum pulse width the filter recognizes as a valid high or low state transition.
	static public final int DAQmx_DI_Tristate  = 0x1890; // Specifies whether to tristate the lines in the channel. If you set this property to TRUE, NI-DAQmx tristates the lines in the channel. If you set this property to FALSE, NI-DAQmx does not modify the configuration of the lines even if the lines were previously tristated. Set this property to FALSE to read lines in other tasks or to read output-only lines.
	static public final int DAQmx_DI_LogicFamily  = 0x296D; // Specifies the logic family to use for acquisition. A logic family corresponds to voltage thresholds that are compatible with a group of voltage standards. Refer to device documentation for information on the logic high and logic low voltages for these logic families.
	static public final int DAQmx_DI_DataXferMech  = 0x2263; // Specifies the data transfer mode for the device.
	static public final int DAQmx_DI_DataXferReqCond  = 0x2264; // Specifies under what condition to transfer data from the onboard memory of the device to the buffer.
	static public final int DAQmx_DI_UsbXferReqSize  = 0x2A90; // Specifies the maximum size of a USB transfer request in bytes. Modify this value to affect performance under different combinations of operating system and device.
	static public final int DAQmx_DI_MemMapEnable  = 0x296A; // Specifies for NI-DAQmx to map hardware registers to the memory space of the application, if possible. Normally, NI-DAQmx maps hardware registers to memory accessible only to the kernel. Mapping the registers to the memory space of the application increases performance. However, if the application accesses the memory space mapped to the registers, it can adversely affect the operation of the device and possibly res...
	static public final int DAQmx_DI_AcquireOn  = 0x2966; // Specifies on which edge of the sample clock to acquire samples.
	static public final int DAQmx_DO_OutputDriveType  = 0x1137; // Specifies the drive type for digital output channels.
	static public final int DAQmx_DO_InvertLines  = 0x1133; // Specifies whether to invert the lines in the channel. If you set this property to TRUE, the lines are at high logic when off and at low logic when on.
	static public final int DAQmx_DO_NumLines  = 0x2179; // Indicates the number of digital lines in the channel.
	static public final int DAQmx_DO_Tristate  = 0x18F3; // Specifies whether to stop driving the channel and set it to a high-impedance state. You must commit the task for this setting to take effect.
	static public final int DAQmx_DO_LineStates_StartState  = 0x2972; // Specifies the state of the lines in a digital output task when the task starts.
	static public final int DAQmx_DO_LineStates_PausedState  = 0x2967; // Specifies the state of the lines in a digital output task when the task pauses.
	static public final int DAQmx_DO_LineStates_DoneState  = 0x2968; // Specifies the state of the lines in a digital output task when the task completes execution.
	static public final int DAQmx_DO_LogicFamily  = 0x296E; // Specifies the logic family to use for generation. A logic family corresponds to voltage thresholds that are compatible with a group of voltage standards. Refer to device documentation for information on the logic high and logic low voltages for these logic families.
	static public final int DAQmx_DO_Overcurrent_Limit  = 0x2A85; // Specifies the current threshold in Amperes for the channel. A value of 0 means the channel observes no limit. Devices can monitor only a finite number of current thresholds simultaneously. If you attempt to monitor additional thresholds, NI-DAQmx returns an error.
	static public final int DAQmx_DO_Overcurrent_AutoReenable  = 0x2A86; // Specifies whether to automatically reenable channels after they no longer exceed the current limit specified by Current Limit.
	static public final int DAQmx_DO_Overcurrent_ReenablePeriod  = 0x2A87; // Specifies the delay in seconds between the time a channel no longer exceeds the current limit and the reactivation of that channel, if Automatic Re-enable is TRUE.
	static public final int DAQmx_DO_UseOnlyOnBrdMem  = 0x2265; // Specifies whether to write samples directly to the onboard memory of the device, bypassing the memory buffer. Generally, you cannot update onboard memory after you start the task. Onboard memory includes data FIFOs.
	static public final int DAQmx_DO_DataXferMech  = 0x2266; // Specifies the data transfer mode for the device.
	static public final int DAQmx_DO_DataXferReqCond  = 0x2267; // Specifies under what condition to transfer data from the buffer to the onboard memory of the device.
	static public final int DAQmx_DO_UsbXferReqSize  = 0x2A91; // Specifies the maximum size of a USB transfer request in bytes. Modify this value to affect performance under different combinations of operating system and device.
	static public final int DAQmx_DO_MemMapEnable  = 0x296B; // Specifies for NI-DAQmx to map hardware registers to the memory space of the application, if possible. Normally, NI-DAQmx maps hardware registers to memory accessible only to the kernel. Mapping the registers to the memory space of the application increases performance. However, if the application accesses the memory space mapped to the registers, it can adversely affect the operation of the device and possibly res...
	static public final int DAQmx_DO_GenerateOn  = 0x2969; // Specifies on which edge of the sample clock to generate samples.
	static public final int DAQmx_CI_Max  = 0x189C; // Specifies the maximum value you expect to measure. This value is in the units you specify with a units property. When you query this property, it returns the coerced maximum value that the hardware can measure with the current settings.
	static public final int DAQmx_CI_Min  = 0x189D; // Specifies the minimum value you expect to measure. This value is in the units you specify with a units property. When you query this property, it returns the coerced minimum value that the hardware can measure with the current settings.
	static public final int DAQmx_CI_CustomScaleName  = 0x189E; // Specifies the name of a custom scale for the channel.
	static public final int DAQmx_CI_MeasType  = 0x18A0; // Indicates the measurement to take with the channel.
	static public final int DAQmx_CI_Freq_Units  = 0x18A1; // Specifies the units to use to return frequency measurements.
	static public final int DAQmx_CI_Freq_Term  = 0x18A2; // Specifies the input terminal of the signal to measure.
	static public final int DAQmx_CI_Freq_StartingEdge  = 0x0799; // Specifies between which edges to measure the frequency of the signal.
	static public final int DAQmx_CI_Freq_MeasMeth  = 0x0144; // Specifies the method to use to measure the frequency of the signal.
	static public final int DAQmx_CI_Freq_MeasTime  = 0x0145; // Specifies in seconds the length of time to measure the frequency of the signal if Method is DAQmx_Val_HighFreq2Ctr. Measurement accuracy increases with increased measurement time and with increased signal frequency. If you measure a high-frequency signal for too long, however, the count register could roll over, which results in an incorrect measurement.
	static public final int DAQmx_CI_Freq_Div  = 0x0147; // Specifies the value by which to divide the input signal if Method is DAQmx_Val_LargeRng2Ctr. The larger the divisor, the more accurate the measurement. However, too large a value could cause the count register to roll over, which results in an incorrect measurement.
	static public final int DAQmx_CI_Freq_DigFltr_Enable  = 0x21E7; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_Freq_DigFltr_MinPulseWidth  = 0x21E8; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_Freq_DigFltr_TimebaseSrc  = 0x21E9; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_Freq_DigFltr_TimebaseRate  = 0x21EA; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_Freq_DigSync_Enable  = 0x21EB; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_Period_Units  = 0x18A3; // Specifies the unit to use to return period measurements.
	static public final int DAQmx_CI_Period_Term  = 0x18A4; // Specifies the input terminal of the signal to measure.
	static public final int DAQmx_CI_Period_StartingEdge  = 0x0852; // Specifies between which edges to measure the period of the signal.
	static public final int DAQmx_CI_Period_MeasMeth  = 0x192C; // Specifies the method to use to measure the period of the signal.
	static public final int DAQmx_CI_Period_MeasTime  = 0x192D; // Specifies in seconds the length of time to measure the period of the signal if Method is DAQmx_Val_HighFreq2Ctr. Measurement accuracy increases with increased measurement time and with increased signal frequency. If you measure a high-frequency signal for too long, however, the count register could roll over, which results in an incorrect measurement.
	static public final int DAQmx_CI_Period_Div  = 0x192E; // Specifies the value by which to divide the input signal if Method is DAQmx_Val_LargeRng2Ctr. The larger the divisor, the more accurate the measurement. However, too large a value could cause the count register to roll over, which results in an incorrect measurement.
	static public final int DAQmx_CI_Period_DigFltr_Enable  = 0x21EC; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_Period_DigFltr_MinPulseWidth  = 0x21ED; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_Period_DigFltr_TimebaseSrc  = 0x21EE; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_Period_DigFltr_TimebaseRate  = 0x21EF; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_Period_DigSync_Enable  = 0x21F0; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_CountEdges_Term  = 0x18C7; // Specifies the input terminal of the signal to measure.
	static public final int DAQmx_CI_CountEdges_Dir  = 0x0696; // Specifies whether to increment or decrement the counter on each edge.
	static public final int DAQmx_CI_CountEdges_DirTerm  = 0x21E1; // Specifies the source terminal of the digital signal that controls the count direction if Direction is DAQmx_Val_ExtControlled.
	static public final int DAQmx_CI_CountEdges_CountDir_DigFltr_Enable  = 0x21F1; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_CountEdges_CountDir_DigFltr_MinPulseWidth  = 0x21F2; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_CountEdges_CountDir_DigFltr_TimebaseSrc  = 0x21F3; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_CountEdges_CountDir_DigFltr_TimebaseRate = 0x21F4; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_CountEdges_CountDir_DigSync_Enable  = 0x21F5; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_CountEdges_InitialCnt  = 0x0698; // Specifies the starting value from which to count.
	static public final int DAQmx_CI_CountEdges_ActiveEdge  = 0x0697; // Specifies on which edges to increment or decrement the counter.
	static public final int DAQmx_CI_CountEdges_DigFltr_Enable  = 0x21F6; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_CountEdges_DigFltr_MinPulseWidth  = 0x21F7; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_CountEdges_DigFltr_TimebaseSrc  = 0x21F8; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_CountEdges_DigFltr_TimebaseRate  = 0x21F9; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_CountEdges_DigSync_Enable  = 0x21FA; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_AngEncoder_Units  = 0x18A6; // Specifies the units to use to return angular position measurements from the channel.
	static public final int DAQmx_CI_AngEncoder_PulsesPerRev  = 0x0875; // Specifies the number of pulses the encoder generates per revolution. This value is the number of pulses on either signal A or signal B, not the total number of pulses on both signal A and signal B.
	static public final int DAQmx_CI_AngEncoder_InitialAngle  = 0x0881; // Specifies the starting angle of the encoder. This value is in the units you specify with Units.
	static public final int DAQmx_CI_LinEncoder_Units  = 0x18A9; // Specifies the units to use to return linear encoder measurements from the channel.
	static public final int DAQmx_CI_LinEncoder_DistPerPulse  = 0x0911; // Specifies the distance to measure for each pulse the encoder generates on signal A or signal B. This value is in the units you specify with Units.
	static public final int DAQmx_CI_LinEncoder_InitialPos  = 0x0915; // Specifies the position of the encoder when the measurement begins. This value is in the units you specify with Units.
	static public final int DAQmx_CI_Encoder_DecodingType  = 0x21E6; // Specifies how to count and interpret the pulses the encoder generates on signal A and signal B. DAQmx_Val_X1, DAQmx_Val_X2, and DAQmx_Val_X4 are valid for quadrature encoders only. DAQmx_Val_TwoPulseCounting is valid for two-pulse encoders only.
	static public final int DAQmx_CI_Encoder_AInputTerm  = 0x219D; // Specifies the terminal to which signal A is connected.
	static public final int DAQmx_CI_Encoder_AInput_DigFltr_Enable  = 0x21FB; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_Encoder_AInput_DigFltr_MinPulseWidth = 0x21FC; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_Encoder_AInput_DigFltr_TimebaseSrc  = 0x21FD; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_Encoder_AInput_DigFltr_TimebaseRate = 0x21FE; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_Encoder_AInput_DigSync_Enable  = 0x21FF; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_Encoder_BInputTerm  = 0x219E; // Specifies the terminal to which signal B is connected.
	static public final int DAQmx_CI_Encoder_BInput_DigFltr_Enable  = 0x2200; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_Encoder_BInput_DigFltr_MinPulseWidth = 0x2201; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_Encoder_BInput_DigFltr_TimebaseSrc  = 0x2202; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_Encoder_BInput_DigFltr_TimebaseRate = 0x2203; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_Encoder_BInput_DigSync_Enable  = 0x2204; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_Encoder_ZInputTerm  = 0x219F; // Specifies the terminal to which signal Z is connected.
	static public final int DAQmx_CI_Encoder_ZInput_DigFltr_Enable  = 0x2205; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_Encoder_ZInput_DigFltr_MinPulseWidth = 0x2206; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_Encoder_ZInput_DigFltr_TimebaseSrc  = 0x2207; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_Encoder_ZInput_DigFltr_TimebaseRate = 0x2208; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_Encoder_ZInput_DigSync_Enable  = 0x2209; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_Encoder_ZIndexEnable  = 0x0890; // Specifies whether to use Z indexing for the channel.
	static public final int DAQmx_CI_Encoder_ZIndexVal  = 0x0888; // Specifies the value to which to reset the measurement when signal Z is high and signal A and signal B are at the states you specify with Z Index Phase. Specify this value in the units of the measurement.
	static public final int DAQmx_CI_Encoder_ZIndexPhase  = 0x0889; // Specifies the states at which signal A and signal B must be while signal Z is high for NI-DAQmx to reset the measurement. If signal Z is never high while signal A and signal B are high, for example, you must choose a phase other than DAQmx_Val_AHighBHigh.
	static public final int DAQmx_CI_PulseWidth_Units  = 0x0823; // Specifies the units to use to return pulse width measurements.
	static public final int DAQmx_CI_PulseWidth_Term  = 0x18AA; // Specifies the input terminal of the signal to measure.
	static public final int DAQmx_CI_PulseWidth_StartingEdge  = 0x0825; // Specifies on which edge of the input signal to begin each pulse width measurement.
	static public final int DAQmx_CI_PulseWidth_DigFltr_Enable  = 0x220A; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_PulseWidth_DigFltr_MinPulseWidth  = 0x220B; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_PulseWidth_DigFltr_TimebaseSrc  = 0x220C; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_PulseWidth_DigFltr_TimebaseRate  = 0x220D; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_PulseWidth_DigSync_Enable  = 0x220E; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_TwoEdgeSep_Units  = 0x18AC; // Specifies the units to use to return two-edge separation measurements from the channel.
	static public final int DAQmx_CI_TwoEdgeSep_FirstTerm  = 0x18AD; // Specifies the source terminal of the digital signal that starts each measurement.
	static public final int DAQmx_CI_TwoEdgeSep_FirstEdge  = 0x0833; // Specifies on which edge of the first signal to start each measurement.
	static public final int DAQmx_CI_TwoEdgeSep_First_DigFltr_Enable  = 0x220F; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_TwoEdgeSep_First_DigFltr_MinPulseWidth = 0x2210; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_TwoEdgeSep_First_DigFltr_TimebaseSrc = 0x2211; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_TwoEdgeSep_First_DigFltr_TimebaseRate = 0x2212; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_TwoEdgeSep_First_DigSync_Enable  = 0x2213; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_TwoEdgeSep_SecondTerm  = 0x18AE; // Specifies the source terminal of the digital signal that stops each measurement.
	static public final int DAQmx_CI_TwoEdgeSep_SecondEdge  = 0x0834; // Specifies on which edge of the second signal to stop each measurement.
	static public final int DAQmx_CI_TwoEdgeSep_Second_DigFltr_Enable  = 0x2214; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_TwoEdgeSep_Second_DigFltr_MinPulseWidth = 0x2215; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_TwoEdgeSep_Second_DigFltr_TimebaseSrc = 0x2216; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_TwoEdgeSep_Second_DigFltr_TimebaseRate = 0x2217; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_TwoEdgeSep_Second_DigSync_Enable  = 0x2218; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_SemiPeriod_Units  = 0x18AF; // Specifies the units to use to return semi-period measurements.
	static public final int DAQmx_CI_SemiPeriod_Term  = 0x18B0; // Specifies the input terminal of the signal to measure.
	static public final int DAQmx_CI_SemiPeriod_StartingEdge  = 0x22FE; // Specifies on which edge of the input signal to begin semi-period measurement. Semi-period measurements alternate between high time and low time, starting on this edge.
	static public final int DAQmx_CI_SemiPeriod_DigFltr_Enable  = 0x2219; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_SemiPeriod_DigFltr_MinPulseWidth  = 0x221A; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_SemiPeriod_DigFltr_TimebaseSrc  = 0x221B; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_SemiPeriod_DigFltr_TimebaseRate  = 0x221C; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_SemiPeriod_DigSync_Enable  = 0x221D; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_Timestamp_Units  = 0x22B3; // Specifies the units to use to return timestamp measurements.
	static public final int DAQmx_CI_Timestamp_InitialSeconds  = 0x22B4; // Specifies the number of seconds that elapsed since the beginning of the current year. This value is ignored if Synchronization Method is DAQmx_Val_IRIGB.
	static public final int DAQmx_CI_GPS_SyncMethod  = 0x1092; // Specifies the method to use to synchronize the counter to a GPS receiver.
	static public final int DAQmx_CI_GPS_SyncSrc  = 0x1093; // Specifies the terminal to which the GPS synchronization signal is connected.
	static public final int DAQmx_CI_CtrTimebaseSrc  = 0x0143; // Specifies the terminal of the timebase to use for the counter.
	static public final int DAQmx_CI_CtrTimebaseRate  = 0x18B2; // Specifies in Hertz the frequency of the counter timebase. Specifying the rate of a counter timebase allows you to take measurements in terms of time or frequency rather than in ticks of the timebase. If you use an external timebase and do not specify the rate, you can take measurements only in terms of ticks of the timebase.
	static public final int DAQmx_CI_CtrTimebaseActiveEdge  = 0x0142; // Specifies whether a timebase cycle is from rising edge to rising edge or from falling edge to falling edge.
	static public final int DAQmx_CI_CtrTimebase_DigFltr_Enable  = 0x2271; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CI_CtrTimebase_DigFltr_MinPulseWidth  = 0x2272; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CI_CtrTimebase_DigFltr_TimebaseSrc  = 0x2273; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CI_CtrTimebase_DigFltr_TimebaseRate  = 0x2274; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CI_CtrTimebase_DigSync_Enable  = 0x2275; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CI_Count  = 0x0148; // Indicates the current value of the count register.
	static public final int DAQmx_CI_OutputState  = 0x0149; // Indicates the current state of the out terminal of the counter.
	static public final int DAQmx_CI_TCReached  = 0x0150; // Indicates whether the counter rolled over. When you query this property, NI-DAQmx resets it to FALSE.
	static public final int DAQmx_CI_CtrTimebaseMasterTimebaseDiv  = 0x18B3; // Specifies the divisor for an external counter timebase. You can divide the counter timebase in order to measure slower signals without causing the count register to roll over.
	static public final int DAQmx_CI_DataXferMech  = 0x0200; // Specifies the data transfer mode for the channel.
	static public final int DAQmx_CI_UsbXferReqSize  = 0x2A92; // Specifies the maximum size of a USB transfer request in bytes. Modify this value to affect performance under different combinations of operating system and device.
	static public final int DAQmx_CI_NumPossiblyInvalidSamps  = 0x193C; // Indicates the number of samples that the device might have overwritten before it could transfer them to the buffer.
	static public final int DAQmx_CI_DupCountPrevent  = 0x21AC; // Specifies whether to enable duplicate count prevention for the channel. Duplicate count prevention is enabled by default. Setting Prescaler disables duplicate count prevention unless you explicitly enable it.
	static public final int DAQmx_CI_Prescaler  = 0x2239; // Specifies the divisor to apply to the signal you connect to the counter source terminal. Scaled data that you read takes this setting into account. You should use a prescaler only when you connect an external signal to the counter source terminal and when that signal has a higher frequency than the fastest onboard timebase. Setting this value disables duplicate count prevention unless you explicitly set Duplicate ...
	static public final int DAQmx_CO_OutputType  = 0x18B5; // Indicates how to define pulses generated on the channel.
	static public final int DAQmx_CO_Pulse_IdleState  = 0x1170; // Specifies the resting state of the output terminal.
	static public final int DAQmx_CO_Pulse_Term  = 0x18E1; // Specifies on which terminal to generate pulses.
	static public final int DAQmx_CO_Pulse_Time_Units  = 0x18D6; // Specifies the units in which to define high and low pulse time.
	static public final int DAQmx_CO_Pulse_HighTime  = 0x18BA; // Specifies the amount of time that the pulse is at a high voltage. This value is in the units you specify with Units or when you create the channel.
	static public final int DAQmx_CO_Pulse_LowTime  = 0x18BB; // Specifies the amount of time that the pulse is at a low voltage. This value is in the units you specify with Units or when you create the channel.
	static public final int DAQmx_CO_Pulse_Time_InitialDelay  = 0x18BC; // Specifies in seconds the amount of time to wait before generating the first pulse.
	static public final int DAQmx_CO_Pulse_DutyCyc  = 0x1176; // Specifies the duty cycle of the pulses. The duty cycle of a signal is the width of the pulse divided by period. NI-DAQmx uses this ratio and the pulse frequency to determine the width of the pulses and the delay between pulses.
	static public final int DAQmx_CO_Pulse_Freq_Units  = 0x18D5; // Specifies the units in which to define pulse frequency.
	static public final int DAQmx_CO_Pulse_Freq  = 0x1178; // Specifies the frequency of the pulses to generate. This value is in the units you specify with Units or when you create the channel.
	static public final int DAQmx_CO_Pulse_Freq_InitialDelay  = 0x0299; // Specifies in seconds the amount of time to wait before generating the first pulse.
	static public final int DAQmx_CO_Pulse_HighTicks  = 0x1169; // Specifies the number of ticks the pulse is high.
	static public final int DAQmx_CO_Pulse_LowTicks  = 0x1171; // Specifies the number of ticks the pulse is low.
	static public final int DAQmx_CO_Pulse_Ticks_InitialDelay  = 0x0298; // Specifies the number of ticks to wait before generating the first pulse.
	static public final int DAQmx_CO_CtrTimebaseSrc  = 0x0339; // Specifies the terminal of the timebase to use for the counter. Typically, NI-DAQmx uses one of the internal counter timebases when generating pulses. Use this property to specify an external timebase and produce custom pulse widths that are not possible using the internal timebases.
	static public final int DAQmx_CO_CtrTimebaseRate  = 0x18C2; // Specifies in Hertz the frequency of the counter timebase. Specifying the rate of a counter timebase allows you to define output pulses in seconds rather than in ticks of the timebase. If you use an external timebase and do not specify the rate, you can define output pulses only in ticks of the timebase.
	static public final int DAQmx_CO_CtrTimebaseActiveEdge  = 0x0341; // Specifies whether a timebase cycle is from rising edge to rising edge or from falling edge to falling edge.
	static public final int DAQmx_CO_CtrTimebase_DigFltr_Enable  = 0x2276; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_CO_CtrTimebase_DigFltr_MinPulseWidth  = 0x2277; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_CO_CtrTimebase_DigFltr_TimebaseSrc  = 0x2278; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_CO_CtrTimebase_DigFltr_TimebaseRate  = 0x2279; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_CO_CtrTimebase_DigSync_Enable  = 0x227A; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_CO_Count  = 0x0293; // Indicates the current value of the count register.
	static public final int DAQmx_CO_OutputState  = 0x0294; // Indicates the current state of the output terminal of the counter.
	static public final int DAQmx_CO_AutoIncrCnt  = 0x0295; // Specifies a number of timebase ticks by which to increment each successive pulse.
	static public final int DAQmx_CO_CtrTimebaseMasterTimebaseDiv  = 0x18C3; // Specifies the divisor for an external counter timebase. You can divide the counter timebase in order to generate slower signals without causing the count register to roll over.
	static public final int DAQmx_CO_PulseDone  = 0x190E; // Indicates if the task completed pulse generation. Use this value for retriggerable pulse generation when you need to determine if the device generated the current pulse. When you query this property, NI-DAQmx resets it to FALSE.
	static public final int DAQmx_CO_ConstrainedGenMode  = 0x29F2; // Specifies constraints to apply when the counter generates pulses. Constraining the counter reduces the device resources required for counter operation. Constraining the counter can also allow additional analog or counter tasks on the device to run concurrently. For continuous counter tasks, NI-DAQmx consumes no device resources when the counter is constrained. For finite counter tasks, resource use increases with ...
	static public final int DAQmx_CO_Prescaler  = 0x226D; // Specifies the divisor to apply to the signal you connect to the counter source terminal. Pulse generations defined by frequency or time take this setting into account, but pulse generations defined by ticks do not. You should use a prescaler only when you connect an external signal to the counter source terminal and when that signal has a higher frequency than the fastest onboard timebase.
	static public final int DAQmx_CO_RdyForNewVal  = 0x22FF; // Indicates whether the counter is ready for new continuous pulse train values.
	static public final int DAQmx_ChanType  = 0x187F; // Indicates the type of the virtual channel.
	static public final int DAQmx_PhysicalChanName  = 0x18F5; // Specifies the name of the physical channel upon which this virtual channel is based.
	static public final int DAQmx_ChanDescr  = 0x1926; // Specifies a user-defined description for the channel.
	static public final int DAQmx_ChanIsGlobal  = 0x2304; // Indicates whether the channel is a global channel.

	//********** Export Signal Attributes **********
	static public final int DAQmx_Exported_AIConvClk_OutputTerm  = 0x1687; // Specifies the terminal to which to route the AI Convert Clock.
	static public final int DAQmx_Exported_AIConvClk_Pulse_Polarity  = 0x1688; // Indicates the polarity of the exported AI Convert Clock. The polarity is fixed and independent of the active edge of the source of the AI Convert Clock.
	static public final int DAQmx_Exported_10MHzRefClk_OutputTerm  = 0x226E; // Specifies the terminal to which to route the 10MHz Clock.
	static public final int DAQmx_Exported_20MHzTimebase_OutputTerm  = 0x1657; // Specifies the terminal to which to route the 20MHz Timebase.
	static public final int DAQmx_Exported_SampClk_OutputBehavior  = 0x186B; // Specifies whether the exported Sample Clock issues a pulse at the beginning of a sample or changes to a high state for the duration of the sample.
	static public final int DAQmx_Exported_SampClk_OutputTerm  = 0x1663; // Specifies the terminal to which to route the Sample Clock.
	static public final int DAQmx_Exported_SampClk_DelayOffset  = 0x21C4; // Specifies in seconds the amount of time to offset the exported Sample clock. Refer to timing diagrams for generation applications in the device documentation for more information about this value.
	static public final int DAQmx_Exported_SampClk_Pulse_Polarity  = 0x1664; // Specifies the polarity of the exported Sample Clock if Output Behavior is DAQmx_Val_Pulse.
	static public final int DAQmx_Exported_SampClkTimebase_OutputTerm  = 0x18F9; // Specifies the terminal to which to route the Sample Clock Timebase.
	static public final int DAQmx_Exported_DividedSampClkTimebase_OutputTerm = 0x21A1; // Specifies the terminal to which to route the Divided Sample Clock Timebase.
	static public final int DAQmx_Exported_AdvTrig_OutputTerm  = 0x1645; // Specifies the terminal to which to route the Advance Trigger.
	static public final int DAQmx_Exported_AdvTrig_Pulse_Polarity  = 0x1646; // Indicates the polarity of the exported Advance Trigger.
	static public final int DAQmx_Exported_AdvTrig_Pulse_WidthUnits  = 0x1647; // Specifies the units of Width Value.
	static public final int DAQmx_Exported_AdvTrig_Pulse_Width  = 0x1648; // Specifies the width of an exported Advance Trigger pulse. Specify this value in the units you specify with Width Units.
	static public final int DAQmx_Exported_PauseTrig_OutputTerm  = 0x1615; // Specifies the terminal to which to route the Pause Trigger.
	static public final int DAQmx_Exported_PauseTrig_Lvl_ActiveLvl  = 0x1616; // Specifies the active level of the exported Pause Trigger.
	static public final int DAQmx_Exported_RefTrig_OutputTerm  = 0x0590; // Specifies the terminal to which to route the Reference Trigger.
	static public final int DAQmx_Exported_RefTrig_Pulse_Polarity  = 0x0591; // Specifies the polarity of the exported Reference Trigger.
	static public final int DAQmx_Exported_StartTrig_OutputTerm  = 0x0584; // Specifies the terminal to which to route the Start Trigger.
	static public final int DAQmx_Exported_StartTrig_Pulse_Polarity  = 0x0585; // Specifies the polarity of the exported Start Trigger.
	static public final int DAQmx_Exported_AdvCmpltEvent_OutputTerm  = 0x1651; // Specifies the terminal to which to route the Advance Complete Event.
	static public final int DAQmx_Exported_AdvCmpltEvent_Delay  = 0x1757; // Specifies the output signal delay in periods of the sample clock.
	static public final int DAQmx_Exported_AdvCmpltEvent_Pulse_Polarity  = 0x1652; // Specifies the polarity of the exported Advance Complete Event.
	static public final int DAQmx_Exported_AdvCmpltEvent_Pulse_Width  = 0x1654; // Specifies the width of the exported Advance Complete Event pulse.
	static public final int DAQmx_Exported_AIHoldCmpltEvent_OutputTerm  = 0x18ED; // Specifies the terminal to which to route the AI Hold Complete Event.
	static public final int DAQmx_Exported_AIHoldCmpltEvent_PulsePolarity = 0x18EE; // Specifies the polarity of an exported AI Hold Complete Event pulse.
	static public final int DAQmx_Exported_ChangeDetectEvent_OutputTerm  = 0x2197; // Specifies the terminal to which to route the Change Detection Event.
	static public final int DAQmx_Exported_ChangeDetectEvent_Pulse_Polarity = 0x2303; // Specifies the polarity of an exported Change Detection Event pulse.
	static public final int DAQmx_Exported_CtrOutEvent_OutputTerm  = 0x1717; // Specifies the terminal to which to route the Counter Output Event.
	static public final int DAQmx_Exported_CtrOutEvent_OutputBehavior  = 0x174F; // Specifies whether the exported Counter Output Event pulses or changes from one state to the other when the counter reaches terminal count.
	static public final int DAQmx_Exported_CtrOutEvent_Pulse_Polarity  = 0x1718; // Specifies the polarity of the pulses at the output terminal of the counter when Output Behavior is DAQmx_Val_Pulse. NI-DAQmx ignores this property if Output Behavior is DAQmx_Val_Toggle.
	static public final int DAQmx_Exported_CtrOutEvent_Toggle_IdleState  = 0x186A; // Specifies the initial state of the output terminal of the counter when Output Behavior is DAQmx_Val_Toggle. The terminal enters this state when NI-DAQmx commits the task.
	static public final int DAQmx_Exported_HshkEvent_OutputTerm  = 0x22BA; // Specifies the terminal to which to route the Handshake Event.
	static public final int DAQmx_Exported_HshkEvent_OutputBehavior  = 0x22BB; // Specifies the output behavior of the Handshake Event.
	static public final int DAQmx_Exported_HshkEvent_Delay  = 0x22BC; // Specifies the number of seconds to delay after the Handshake Trigger deasserts before asserting the Handshake Event.
	static public final int DAQmx_Exported_HshkEvent_Interlocked_AssertedLvl = 0x22BD; // Specifies the asserted level of the exported Handshake Event if Output Behavior is DAQmx_Val_Interlocked.
	static public final int DAQmx_Exported_HshkEvent_Interlocked_AssertOnStart = 0x22BE; // Specifies to assert the Handshake Event when the task starts if Output Behavior is DAQmx_Val_Interlocked.
	static public final int DAQmx_Exported_HshkEvent_Interlocked_DeassertDelay = 0x22BF; // Specifies in seconds the amount of time to wait after the Handshake Trigger asserts before deasserting the Handshake Event if Output Behavior is DAQmx_Val_Interlocked.
	static public final int DAQmx_Exported_HshkEvent_Pulse_Polarity  = 0x22C0; // Specifies the polarity of the exported Handshake Event if Output Behavior is DAQmx_Val_Pulse.
	static public final int DAQmx_Exported_HshkEvent_Pulse_Width  = 0x22C1; // Specifies in seconds the pulse width of the exported Handshake Event if Output Behavior is DAQmx_Val_Pulse.
	static public final int DAQmx_Exported_RdyForXferEvent_OutputTerm  = 0x22B5; // Specifies the terminal to which to route the Ready for Transfer Event.
	static public final int DAQmx_Exported_RdyForXferEvent_Lvl_ActiveLvl = 0x22B6; // Specifies the active level of the exported Ready for Transfer Event.
	static public final int DAQmx_Exported_RdyForXferEvent_DeassertCond  = 0x2963; // Specifies when the ready for transfer event deasserts.
	static public final int DAQmx_Exported_RdyForXferEvent_DeassertCondCustomThreshold = 0x2964; // Specifies in samples the threshold below which the Ready for Transfer Event deasserts. This threshold is an amount of space available in the onboard memory of the device. Deassert Condition must be DAQmx_Val_OnbrdMemCustomThreshold to use a custom threshold.
	static public final int DAQmx_Exported_DataActiveEvent_OutputTerm  = 0x1633; // Specifies the terminal to which to export the Data Active Event.
	static public final int DAQmx_Exported_DataActiveEvent_Lvl_ActiveLvl = 0x1634; // Specifies the polarity of the exported Data Active Event.
	static public final int DAQmx_Exported_RdyForStartEvent_OutputTerm  = 0x1609; // Specifies the terminal to which to route the Ready for Start Event.
	static public final int DAQmx_Exported_RdyForStartEvent_Lvl_ActiveLvl = 0x1751; // Specifies the polarity of the exported Ready for Start Event.
	static public final int DAQmx_Exported_SyncPulseEvent_OutputTerm  = 0x223C; // Specifies the terminal to which to route the Synchronization Pulse Event.
	static public final int DAQmx_Exported_WatchdogExpiredEvent_OutputTerm = 0x21AA; // Specifies the terminal to which to route the Watchdog Timer Expired Event.

	//********** Device Attributes **********
	static public final int DAQmx_Dev_IsSimulated  = 0x22CA; // Indicates if the device is a simulated device.
	static public final int DAQmx_Dev_ProductCategory  = 0x29A9; // Indicates the product category of the device. This category corresponds to the category displayed in MAX when creating NI-DAQmx simulated devices.
	static public final int DAQmx_Dev_ProductType  = 0x0631; // Indicates the product name of the device.
	static public final int DAQmx_Dev_ProductNum  = 0x231D; // Indicates the unique hardware identification number for the device.
	static public final int DAQmx_Dev_SerialNum  = 0x0632; // Indicates the serial number of the device. This value is zero if the device does not have a serial number.
	static public final int DAQmx_Carrier_SerialNum  = 0x2A8A; // Indicates the serial number of the device carrier. This value is zero if the carrier does not have a serial number.
	static public final int DAQmx_Dev_Chassis_ModuleDevNames  = 0x29B6; // Indicates an array containing the names of the modules in the chassis.
	static public final int DAQmx_Dev_AnlgTrigSupported  = 0x2984; // Indicates if the device supports analog triggering.
	static public final int DAQmx_Dev_DigTrigSupported  = 0x2985; // Indicates if the device supports digital triggering.
	static public final int DAQmx_Dev_AI_PhysicalChans  = 0x231E; // Indicates an array containing the names of the analog input physical channels available on the device.
	static public final int DAQmx_Dev_AI_MaxSingleChanRate  = 0x298C; // Indicates the maximum rate for an analog input task if the task contains only a single channel from this device.
	static public final int DAQmx_Dev_AI_MaxMultiChanRate  = 0x298D; // Indicates the maximum rate for an analog input task if the task contains multiple channels from this device. For multiplexed devices, divide this rate by the number of channels to determine the maximum sampling rate.
	static public final int DAQmx_Dev_AI_MinRate  = 0x298E; // Indicates the minimum rate for an analog input task on this device. NI-DAQmx returns a warning or error if you attempt to sample at a slower rate.
	static public final int DAQmx_Dev_AI_SimultaneousSamplingSupported  = 0x298F; // Indicates if the device supports simultaneous sampling.
	static public final int DAQmx_Dev_AI_TrigUsage  = 0x2986; // Indicates the triggers supported by this device for an analog input task.
	static public final int DAQmx_Dev_AI_VoltageRngs  = 0x2990; // Indicates pairs of input voltage ranges supported by this device. Each pair consists of the low value, followed by the high value.
	static public final int DAQmx_Dev_AI_VoltageIntExcitDiscreteVals  = 0x29C9; // Indicates the set of discrete internal voltage excitation values supported by this device. If the device supports ranges of internal excitation values, use Range Values to determine supported excitation values.
	static public final int DAQmx_Dev_AI_VoltageIntExcitRangeVals  = 0x29CA; // Indicates pairs of internal voltage excitation ranges supported by this device. Each pair consists of the low value, followed by the high value. If the device supports a set of discrete internal excitation values, use Discrete Values to determine the supported excitation values.
	static public final int DAQmx_Dev_AI_CurrentRngs  = 0x2991; // Indicates the pairs of current input ranges supported by this device. Each pair consists of the low value, followed by the high value.
	static public final int DAQmx_Dev_AI_CurrentIntExcitDiscreteVals  = 0x29CB; // Indicates the set of discrete internal current excitation values supported by this device.
	static public final int DAQmx_Dev_AI_FreqRngs  = 0x2992; // Indicates the pairs of frequency input ranges supported by this device. Each pair consists of the low value, followed by the high value.
	static public final int DAQmx_Dev_AI_Gains  = 0x2993; // Indicates the input gain settings supported by this device.
	static public final int DAQmx_Dev_AI_Couplings  = 0x2994; // Indicates the coupling types supported by this device.
	static public final int DAQmx_Dev_AI_LowpassCutoffFreqDiscreteVals  = 0x2995; // Indicates the set of discrete lowpass cutoff frequencies supported by this device. If the device supports ranges of lowpass cutoff frequencies, use Range Values to determine supported frequencies.
	static public final int DAQmx_Dev_AI_LowpassCutoffFreqRangeVals  = 0x29CF; // Indicates pairs of lowpass cutoff frequency ranges supported by this device. Each pair consists of the low value, followed by the high value. If the device supports a set of discrete lowpass cutoff frequencies, use Discrete Values to determine the supported frequencies.
	static public final int DAQmx_Dev_AO_PhysicalChans  = 0x231F; // Indicates an array containing the names of the analog output physical channels available on the device.
	static public final int DAQmx_Dev_AO_SampClkSupported  = 0x2996; // Indicates if the device supports the sample clock timing type for analog output tasks.
	static public final int DAQmx_Dev_AO_MaxRate  = 0x2997; // Indicates the maximum analog output rate of the device.
	static public final int DAQmx_Dev_AO_MinRate  = 0x2998; // Indicates the minimum analog output rate of the device.
	static public final int DAQmx_Dev_AO_TrigUsage  = 0x2987; // Indicates the triggers supported by this device for analog output tasks.
	static public final int DAQmx_Dev_AO_VoltageRngs  = 0x299B; // Indicates pairs of output voltage ranges supported by this device. Each pair consists of the low value, followed by the high value.
	static public final int DAQmx_Dev_AO_CurrentRngs  = 0x299C; // Indicates pairs of output current ranges supported by this device. Each pair consists of the low value, followed by the high value.
	static public final int DAQmx_Dev_AO_Gains  = 0x299D; // Indicates the output gain settings supported by this device.
	static public final int DAQmx_Dev_DI_Lines  = 0x2320; // Indicates an array containing the names of the digital input lines available on the device.
	static public final int DAQmx_Dev_DI_Ports  = 0x2321; // Indicates an array containing the names of the digital input ports available on the device.
	static public final int DAQmx_Dev_DI_MaxRate  = 0x2999; // Indicates the maximum digital input rate of the device.
	static public final int DAQmx_Dev_DI_TrigUsage  = 0x2988; // Indicates the triggers supported by this device for digital input tasks.
	static public final int DAQmx_Dev_DO_Lines  = 0x2322; // Indicates an array containing the names of the digital output lines available on the device.
	static public final int DAQmx_Dev_DO_Ports  = 0x2323; // Indicates an array containing the names of the digital output ports available on the device.
	static public final int DAQmx_Dev_DO_MaxRate  = 0x299A; // Indicates the maximum digital output rate of the device.
	static public final int DAQmx_Dev_DO_TrigUsage  = 0x2989; // Indicates the triggers supported by this device for digital output tasks.
	static public final int DAQmx_Dev_CI_PhysicalChans  = 0x2324; // Indicates an array containing the names of the counter input physical channels available on the device.
	static public final int DAQmx_Dev_CI_TrigUsage  = 0x298A; // Indicates the triggers supported by this device for counter input tasks.
	static public final int DAQmx_Dev_CI_SampClkSupported  = 0x299E; // Indicates if the device supports the sample clock timing type for counter input tasks.
	static public final int DAQmx_Dev_CI_MaxSize  = 0x299F; // Indicates in bits the size of the counters on the device.
	static public final int DAQmx_Dev_CI_MaxTimebase  = 0x29A0; // Indicates in hertz the maximum counter timebase frequency.
	static public final int DAQmx_Dev_CO_PhysicalChans  = 0x2325; // Indicates an array containing the names of the counter output physical channels available on the device.
	static public final int DAQmx_Dev_CO_TrigUsage  = 0x298B; // Indicates the triggers supported by this device for counter output tasks.
	static public final int DAQmx_Dev_CO_MaxSize  = 0x29A1; // Indicates in bits the size of the counters on the device.
	static public final int DAQmx_Dev_CO_MaxTimebase  = 0x29A2; // Indicates in hertz the maximum counter timebase frequency.
	static public final int DAQmx_Dev_NumDMAChans  = 0x233C; // Indicates the number of DMA channels on the device.
	static public final int DAQmx_Dev_BusType  = 0x2326; // Indicates the bus type of the device.
	static public final int DAQmx_Dev_PCI_BusNum  = 0x2327; // Indicates the PCI bus number of the device.
	static public final int DAQmx_Dev_PCI_DevNum  = 0x2328; // Indicates the PCI slot number of the device.
	static public final int DAQmx_Dev_PXI_ChassisNum  = 0x2329; // Indicates the PXI chassis number of the device, as identified in MAX.
	static public final int DAQmx_Dev_PXI_SlotNum  = 0x232A; // Indicates the PXI slot number of the device.
	static public final int DAQmx_Dev_CompactDAQ_ChassisDevName  = 0x29B7; // Indicates the name of the CompactDAQ chassis that contains this module.
	static public final int DAQmx_Dev_CompactDAQ_SlotNum  = 0x29B8; // Indicates the slot number in which this module is located in the CompactDAQ chassis.
	static public final int DAQmx_Dev_TCPIP_Hostname  = 0x2A8B; // Indicates the IPv4 hostname of the device.
	static public final int DAQmx_Dev_TCPIP_EthernetIP  = 0x2A8C; // Indicates the IPv4 address of the Ethernet interface. This property returns an empty string if the Ethernet interface cannot acquire an address.
	static public final int DAQmx_Dev_TCPIP_WirelessIP  = 0x2A8D; // Indicates the IPv4 address of the wireless interface.This property returns an empty string if the wireless interface cannot acquire an address.
	static public final int DAQmx_Dev_Terminals  = 0x2A40; // Indicates a list of all terminals on the device.

	//********** Read Attributes **********
	static public final int DAQmx_Read_RelativeTo  = 0x190A; // Specifies the point in the buffer at which to begin a read operation. If you also specify an offset with Offset, the read operation begins at that offset relative to the point you select with this property. The default value is DAQmx_Val_CurrReadPos unless you configure a Reference Trigger for the task. If you configure a Reference Trigger, the default value is DAQmx_Val_FirstPretrigSamp.
	static public final int DAQmx_Read_Offset  = 0x190B; // Specifies an offset in samples per channel at which to begin a read operation. This offset is relative to the location you specify with RelativeTo.
	static public final int DAQmx_Read_ChannelsToRead  = 0x1823; // Specifies a subset of channels in the task from which to read.
	static public final int DAQmx_Read_ReadAllAvailSamp  = 0x1215; // Specifies whether subsequent read operations read all samples currently available in the buffer or wait for the buffer to become full before reading. NI-DAQmx uses this setting for finite acquisitions and only when the number of samples to read is -1. For continuous acquisitions when the number of samples to read is -1, a read operation always reads all samples currently available in the buffer.
	static public final int DAQmx_Read_AutoStart  = 0x1826; // Specifies if an NI-DAQmx Read function automatically starts the task if you did not start the task explicitly by using DAQmxStartTask(). The default value is TRUE. When an NI-DAQmx Read function starts a finite acquisition task, it also stops the task after reading the last sample.
	static public final int DAQmx_Read_OverWrite  = 0x1211; // Specifies whether to overwrite samples in the buffer that you have not yet read.
	static public final int DAQmx_Read_CurrReadPos  = 0x1221; // Indicates in samples per channel the current position in the buffer.
	static public final int DAQmx_Read_AvailSampPerChan  = 0x1223; // Indicates the number of samples available to read per channel. This value is the same for all channels in the task.
	static public final int DAQmx_Read_TotalSampPerChanAcquired  = 0x192A; // Indicates the total number of samples acquired by each channel. NI-DAQmx returns a single value because this value is the same for all channels.
	static public final int DAQmx_Read_CommonModeRangeErrorChansExist  = 0x2A98; // Indicates if the device(s) detected a common mode range violation for any virtual channel in the task. Common mode range violation occurs when the voltage of either the positive terminal or negative terminal to ground are out of range. Reading this property clears the common mode range violation status for all channels in the task. You must read this property before you read Common Mode Range Error Channels. Other...
	static public final int DAQmx_Read_CommonModeRangeErrorChans  = 0x2A99; // Indicates the names of any virtual channels in the task for which the device(s) detected a common mode range violation. You must read Common Mode Range Error Channels Exist before you read this property. Otherwise, you will receive an error.
	static public final int DAQmx_Read_OvercurrentChansExist  = 0x29E6; // Indicates if the device(s) detected an overcurrent condition for any virtual channel in the task. Reading this property clears the overcurrent status for all channels in the task. You must read this property before you read Overcurrent Channels. Otherwise, you will receive an error.
	static public final int DAQmx_Read_OvercurrentChans  = 0x29E7; // Indicates the names of any virtual channels in the task for which the device(s) detected an overcurrent condition.. You must read Overcurrent Channels Exist before you read this property. Otherwise, you will receive an error. On some devices, you must restart the task for all overcurrent channels to recover.
	static public final int DAQmx_Read_OpenCurrentLoopChansExist  = 0x2A09; // Indicates if the device(s) detected an open current loop for any virtual channel in the task. Reading this property clears the open current loop status for all channels in the task. You must read this property before you read Open Current Loop Channels. Otherwise, you will receive an error.
	static public final int DAQmx_Read_OpenCurrentLoopChans  = 0x2A0A; // Indicates the names of any virtual channels in the task for which the device(s) detected an open current loop. You must read Open Current Loop Channels Exist before you read this property. Otherwise, you will receive an error.
	static public final int DAQmx_Read_OpenThrmcplChansExist  = 0x2A96; // Indicates if the device(s) detected an open thermocouple connected to any virtual channel in the task. Reading this property clears the open thermocouple status for all channels in the task. You must read this property before you read Open Thermocouple Channels. Otherwise, you will receive an error.
	static public final int DAQmx_Read_OpenThrmcplChans  = 0x2A97; // Indicates the names of any virtual channels in the task for which the device(s) detected an open thermcouple. You must read Open Thermocouple Channels Exist before you read this property. Otherwise, you will receive an error.
	static public final int DAQmx_Read_OverloadedChansExist  = 0x2174; // Indicates if the device(s) detected an overload in any virtual channel in the task. Reading this property clears the overload status for all channels in the task. You must read this property before you read Overloaded Channels. Otherwise, you will receive an error.
	static public final int DAQmx_Read_OverloadedChans  = 0x2175; // Indicates the names of any overloaded virtual channels in the task. You must read Overloaded Channels Exist before you read this property. Otherwise, you will receive an error.
	static public final int DAQmx_Read_ChangeDetect_HasOverflowed  = 0x2194; // Indicates if samples were missed because change detection events occurred faster than the device could handle them. Some devices detect overflows differently than others.
	static public final int DAQmx_Read_RawDataWidth  = 0x217A; // Indicates in bytes the size of a raw sample from the task.
	static public final int DAQmx_Read_NumChans  = 0x217B; // Indicates the number of channels that an NI-DAQmx Read function reads from the task. This value is the number of channels in the task or the number of channels you specify with Channels to Read.
	static public final int DAQmx_Read_DigitalLines_BytesPerChan  = 0x217C; // Indicates the number of bytes per channel that NI-DAQmx returns in a sample for line-based reads. If a channel has fewer lines than this number, the extra bytes are FALSE.
	static public final int DAQmx_Read_WaitMode  = 0x2232; // Specifies how an NI-DAQmx Read function waits for samples to become available.
	static public final int DAQmx_Read_SleepTime  = 0x22B0; // Specifies in seconds the amount of time to sleep after checking for available samples if Wait Mode is DAQmx_Val_Sleep.

	//********** Real-Time Attributes **********
	static public final int DAQmx_RealTime_ConvLateErrorsToWarnings  = 0x22EE; // Specifies if DAQmxWaitForNextSampleClock() and an NI-DAQmx Read function convert late errors to warnings. NI-DAQmx returns no late warnings or errors until the number of warmup iterations you specify with Number Of Warmup Iterations execute.
	static public final int DAQmx_RealTime_NumOfWarmupIters  = 0x22ED; // Specifies the number of loop iterations that must occur before DAQmxWaitForNextSampleClock() and an NI-DAQmx Read function return any late warnings or errors. The system needs a number of iterations to stabilize. During this period, a large amount of jitter occurs, potentially causing reads and writes to be late. The default number of warmup iterations is 100. Specify a larger number if needed to stabilize the sys...
	static public final int DAQmx_RealTime_WaitForNextSampClkWaitMode  = 0x22EF; // Specifies how DAQmxWaitForNextSampleClock() waits for the next Sample Clock pulse.
	static public final int DAQmx_RealTime_ReportMissedSamp  = 0x2319; // Specifies whether an NI-DAQmx Read function returns lateness errors or warnings when it detects missed Sample Clock pulses. This setting does not affect DAQmxWaitForNextSampleClock(). Set this property to TRUE for applications that need to detect lateness without using DAQmxWaitForNextSampleClock().
	static public final int DAQmx_RealTime_WriteRecoveryMode  = 0x231A; // Specifies how NI-DAQmx attempts to recover after missing a Sample Clock pulse when performing counter writes.

	//********** Switch Channel Attributes **********
	static public final int DAQmx_SwitchChan_Usage  = 0x18E4; // Specifies how you can use the channel. Using this property acts as a safety mechanism to prevent you from connecting two source channels, for example.
	static public final int DAQmx_SwitchChan_MaxACCarryCurrent  = 0x0648; // Indicates in amperes the maximum AC current that the device can carry.
	static public final int DAQmx_SwitchChan_MaxACSwitchCurrent  = 0x0646; // Indicates in amperes the maximum AC current that the device can switch. This current is always against an RMS voltage level.
	static public final int DAQmx_SwitchChan_MaxACCarryPwr  = 0x0642; // Indicates in watts the maximum AC power that the device can carry.
	static public final int DAQmx_SwitchChan_MaxACSwitchPwr  = 0x0644; // Indicates in watts the maximum AC power that the device can switch.
	static public final int DAQmx_SwitchChan_MaxDCCarryCurrent  = 0x0647; // Indicates in amperes the maximum DC current that the device can carry.
	static public final int DAQmx_SwitchChan_MaxDCSwitchCurrent  = 0x0645; // Indicates in amperes the maximum DC current that the device can switch. This current is always against a DC voltage level.
	static public final int DAQmx_SwitchChan_MaxDCCarryPwr  = 0x0643; // Indicates in watts the maximum DC power that the device can carry.
	static public final int DAQmx_SwitchChan_MaxDCSwitchPwr  = 0x0649; // Indicates in watts the maximum DC power that the device can switch.
	static public final int DAQmx_SwitchChan_MaxACVoltage  = 0x0651; // Indicates in volts the maximum AC RMS voltage that the device can switch.
	static public final int DAQmx_SwitchChan_MaxDCVoltage  = 0x0650; // Indicates in volts the maximum DC voltage that the device can switch.
	static public final int DAQmx_SwitchChan_WireMode  = 0x18E5; // Indicates the number of wires that the channel switches.
	static public final int DAQmx_SwitchChan_Bandwidth  = 0x0640; // Indicates in Hertz the maximum frequency of a signal that can pass through the switch without significant deterioration.
	static public final int DAQmx_SwitchChan_Impedance  = 0x0641; // Indicates in ohms the switch impedance. This value is important in the RF domain and should match the impedance of the sources and loads.

	//********** Switch Device Attributes **********
	static public final int DAQmx_SwitchDev_SettlingTime  = 0x1244; // Specifies in seconds the amount of time to wait for the switch to settle (or debounce). NI-DAQmx adds this time to the settling time of the motherboard. Modify this property only if the switch does not settle within the settling time of the motherboard. Refer to device documentation for supported settling times.
	static public final int DAQmx_SwitchDev_AutoConnAnlgBus  = 0x17DA; // Specifies if NI-DAQmx routes multiplexed channels to the analog bus backplane. Only the SCXI-1127 and SCXI-1128 support this property.
	static public final int DAQmx_SwitchDev_PwrDownLatchRelaysAfterSettling = 0x22DB; // Specifies if DAQmxSwitchWaitForSettling() powers down latching relays after waiting for the device to settle.
	static public final int DAQmx_SwitchDev_Settled  = 0x1243; // Indicates when Settling Time expires.
	static public final int DAQmx_SwitchDev_RelayList  = 0x17DC; // Indicates a comma-delimited list of relay names.
	static public final int DAQmx_SwitchDev_NumRelays  = 0x18E6; // Indicates the number of relays on the device. This value matches the number of relay names in Relay List.
	static public final int DAQmx_SwitchDev_SwitchChanList  = 0x18E7; // Indicates a comma-delimited list of channel names for the current topology of the device.
	static public final int DAQmx_SwitchDev_NumSwitchChans  = 0x18E8; // Indicates the number of switch channels for the current topology of the device. This value matches the number of channel names in Switch Channel List.
	static public final int DAQmx_SwitchDev_NumRows  = 0x18E9; // Indicates the number of rows on a device in a matrix switch topology. Indicates the number of multiplexed channels on a device in a mux topology.
	static public final int DAQmx_SwitchDev_NumColumns  = 0x18EA; // Indicates the number of columns on a device in a matrix switch topology. This value is always 1 if the device is in a mux topology.
	static public final int DAQmx_SwitchDev_Topology  = 0x193D; // Indicates the current topology of the device. This value is one of the topology options in DAQmxSwitchSetTopologyAndReset().

	//********** Switch Scan Attributes **********
	static public final int DAQmx_SwitchScan_BreakMode  = 0x1247; // Specifies the action to take between each entry in a scan list.
	static public final int DAQmx_SwitchScan_RepeatMode  = 0x1248; // Specifies if the task advances through the scan list multiple times.
	static public final int DAQmx_SwitchScan_WaitingForAdv  = 0x17D9; // Indicates if the switch hardware is waiting for an Advance Trigger. If the hardware is waiting, it completed the previous entry in the scan list.

	//********** Scale Attributes **********
	static public final int DAQmx_Scale_Descr  = 0x1226; // Specifies a description for the scale.
	static public final int DAQmx_Scale_ScaledUnits  = 0x191B; // Specifies the units to use for scaled values. You can use an arbitrary string.
	static public final int DAQmx_Scale_PreScaledUnits  = 0x18F7; // Specifies the units of the values that you want to scale.
	static public final int DAQmx_Scale_Type  = 0x1929; // Indicates the method or equation form that the custom scale uses.
	static public final int DAQmx_Scale_Lin_Slope  = 0x1227; // Specifies the slope, m, in the equation y = mx+b.
	static public final int DAQmx_Scale_Lin_YIntercept  = 0x1228; // Specifies the y-intercept, b, in the equation y = mx+b.
	static public final int DAQmx_Scale_Map_ScaledMax  = 0x1229; // Specifies the largest value in the range of scaled values. NI-DAQmx maps this value to Pre-Scaled Maximum Value. Reads coerce samples that are larger than this value to match this value. Writes generate errors for samples that are larger than this value.
	static public final int DAQmx_Scale_Map_PreScaledMax  = 0x1231; // Specifies the largest value in the range of pre-scaled values. NI-DAQmx maps this value to Scaled Maximum Value.
	static public final int DAQmx_Scale_Map_ScaledMin  = 0x1230; // Specifies the smallest value in the range of scaled values. NI-DAQmx maps this value to Pre-Scaled Minimum Value. Reads coerce samples that are smaller than this value to match this value. Writes generate errors for samples that are smaller than this value.
	static public final int DAQmx_Scale_Map_PreScaledMin  = 0x1232; // Specifies the smallest value in the range of pre-scaled values. NI-DAQmx maps this value to Scaled Minimum Value.
	static public final int DAQmx_Scale_Poly_ForwardCoeff  = 0x1234; // Specifies an array of coefficients for the polynomial that converts pre-scaled values to scaled values. Each element of the array corresponds to a term of the equation. For example, if index three of the array is 9, the fourth term of the equation is 9x^3.
	static public final int DAQmx_Scale_Poly_ReverseCoeff  = 0x1235; // Specifies an array of coefficients for the polynomial that converts scaled values to pre-scaled values. Each element of the array corresponds to a term of the equation. For example, if index three of the array is 9, the fourth term of the equation is 9y^3.
	static public final int DAQmx_Scale_Table_ScaledVals  = 0x1236; // Specifies an array of scaled values. These values map directly to the values in Pre-Scaled Values.
	static public final int DAQmx_Scale_Table_PreScaledVals  = 0x1237; // Specifies an array of pre-scaled values. These values map directly to the values in Scaled Values.

	//********** System Attributes **********
	static public final int DAQmx_Sys_GlobalChans  = 0x1265; // Indicates an array that contains the names of all global channels saved on the system.
	static public final int DAQmx_Sys_Scales  = 0x1266; // Indicates an array that contains the names of all custom scales saved on the system.
	static public final int DAQmx_Sys_Tasks  = 0x1267; // Indicates an array that contains the names of all tasks saved on the system.
	static public final int DAQmx_Sys_DevNames  = 0x193B; // Indicates the names of all devices installed in the system.
	static public final int DAQmx_Sys_NIDAQMajorVersion  = 0x1272; // Indicates the major portion of the installed version of NI-DAQ, such as 7 for version 7.0.
	static public final int DAQmx_Sys_NIDAQMinorVersion  = 0x1923; // Indicates the minor portion of the installed version of NI-DAQ, such as 0 for version 7.0.

	//********** Task Attributes **********
	static public final int DAQmx_Task_Name  = 0x1276; // Indicates the name of the task.
	static public final int DAQmx_Task_Channels  = 0x1273; // Indicates the names of all virtual channels in the task.
	static public final int DAQmx_Task_NumChans  = 0x2181; // Indicates the number of virtual channels in the task.
	static public final int DAQmx_Task_Devices  = 0x230E; // Indicates an array containing the names of all devices in the task.
	static public final int DAQmx_Task_NumDevices  = 0x29BA; // Indicates the number of devices in the task.
	static public final int DAQmx_Task_Complete  = 0x1274; // Indicates whether the task completed execution.

	//********** Timing Attributes **********
	static public final int DAQmx_SampQuant_SampMode  = 0x1300; // Specifies if a task acquires or generates a finite number of samples or if it continuously acquires or generates samples.
	static public final int DAQmx_SampQuant_SampPerChan  = 0x1310; // Specifies the number of samples to acquire or generate for each channel if Sample Mode is DAQmx_Val_FiniteSamps. If Sample Mode is DAQmx_Val_ContSamps, NI-DAQmx uses this value to determine the buffer size.
	static public final int DAQmx_SampTimingType  = 0x1347; // Specifies the type of sample timing to use for the task.
	static public final int DAQmx_SampClk_Rate  = 0x1344; // Specifies the sampling rate in samples per channel per second. If you use an external source for the Sample Clock, set this input to the maximum expected rate of that clock.
	static public final int DAQmx_SampClk_MaxRate  = 0x22C8; // Indicates the maximum Sample Clock rate supported by the task, based on other timing settings. For output tasks, the maximum Sample Clock rate is the maximum rate of the DAC. For input tasks, NI-DAQmx calculates the maximum sampling rate differently for multiplexed devices than simultaneous sampling devices.
	static public final int DAQmx_SampClk_Src  = 0x1852; // Specifies the terminal of the signal to use as the Sample Clock.
	static public final int DAQmx_SampClk_ActiveEdge  = 0x1301; // Specifies on which edge of a clock pulse sampling takes place. This property is useful primarily when the signal you use as the Sample Clock is not a periodic clock.
	static public final int DAQmx_SampClk_UnderflowBehavior  = 0x2961; // Specifies the action to take when the onboard memory of the device becomes empty.
	static public final int DAQmx_SampClk_TimebaseDiv  = 0x18EB; // Specifies the number of Sample Clock Timebase pulses needed to produce a single Sample Clock pulse.
	static public final int DAQmx_SampClk_Timebase_Rate  = 0x1303; // Specifies the rate of the Sample Clock Timebase. Some applications require that you specify a rate when you use any signal other than the onboard Sample Clock Timebase. NI-DAQmx requires this rate to calculate other timing parameters.
	static public final int DAQmx_SampClk_Timebase_Src  = 0x1308; // Specifies the terminal of the signal to use as the Sample Clock Timebase.
	static public final int DAQmx_SampClk_Timebase_ActiveEdge  = 0x18EC; // Specifies on which edge to recognize a Sample Clock Timebase pulse. This property is useful primarily when the signal you use as the Sample Clock Timebase is not a periodic clock.
	static public final int DAQmx_SampClk_Timebase_MasterTimebaseDiv  = 0x1305; // Specifies the number of pulses of the Master Timebase needed to produce a single pulse of the Sample Clock Timebase.
	static public final int DAQmx_SampClk_DigFltr_Enable  = 0x221E; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_SampClk_DigFltr_MinPulseWidth  = 0x221F; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_SampClk_DigFltr_TimebaseSrc  = 0x2220; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_SampClk_DigFltr_TimebaseRate  = 0x2221; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_SampClk_DigSync_Enable  = 0x2222; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_Hshk_DelayAfterXfer  = 0x22C2; // Specifies the number of seconds to wait after a handshake cycle before starting a new handshake cycle.
	static public final int DAQmx_Hshk_StartCond  = 0x22C3; // Specifies the point in the handshake cycle that the device is in when the task starts.
	static public final int DAQmx_Hshk_SampleInputDataWhen  = 0x22C4; // Specifies on which edge of the Handshake Trigger an input task latches the data from the peripheral device.
	static public final int DAQmx_ChangeDetect_DI_RisingEdgePhysicalChans = 0x2195; // Specifies the names of the digital lines or ports on which to detect rising edges. The lines or ports must be used by virtual channels in the task. You also can specify a string that contains a list or range of digital lines or ports.
	static public final int DAQmx_ChangeDetect_DI_FallingEdgePhysicalChans = 0x2196; // Specifies the names of the digital lines or ports on which to detect falling edges. The lines or ports must be used by virtual channels in the task. You also can specify a string that contains a list or range of digital lines or ports.
	static public final int DAQmx_OnDemand_SimultaneousAOEnable  = 0x21A0; // Specifies whether to update all channels in the task simultaneously, rather than updating channels independently when you write a sample to that channel.
	static public final int DAQmx_AIConv_Rate  = 0x1848; // Specifies in Hertz the rate at which to clock the analog-to-digital converter. This clock is specific to the analog input section of multiplexed devices.
	static public final int DAQmx_AIConv_MaxRate  = 0x22C9; // Indicates the maximum convert rate supported by the task, given the current devices and channel count.
	static public final int DAQmx_AIConv_Src  = 0x1502; // Specifies the terminal of the signal to use as the AI Convert Clock.
	static public final int DAQmx_AIConv_ActiveEdge  = 0x1853; // Specifies on which edge of the clock pulse an analog-to-digital conversion takes place.
	static public final int DAQmx_AIConv_TimebaseDiv  = 0x1335; // Specifies the number of AI Convert Clock Timebase pulses needed to produce a single AI Convert Clock pulse.
	static public final int DAQmx_AIConv_Timebase_Src  = 0x1339; // Specifies the terminal of the signal to use as the AI Convert Clock Timebase.
	static public final int DAQmx_DelayFromSampClk_DelayUnits  = 0x1304; // Specifies the units of Delay.
	static public final int DAQmx_DelayFromSampClk_Delay  = 0x1317; // Specifies the amount of time to wait after receiving a Sample Clock edge before beginning to acquire the sample. This value is in the units you specify with Delay Units.
	static public final int DAQmx_MasterTimebase_Rate  = 0x1495; // Specifies the rate of the Master Timebase.
	static public final int DAQmx_MasterTimebase_Src  = 0x1343; // Specifies the terminal of the signal to use as the Master Timebase. On an E Series device, you can choose only between the onboard 20MHz Timebase or the RTSI7 terminal.
	static public final int DAQmx_RefClk_Rate  = 0x1315; // Specifies the frequency of the Reference Clock.
	static public final int DAQmx_RefClk_Src  = 0x1316; // Specifies the terminal of the signal to use as the Reference Clock.
	static public final int DAQmx_SyncPulse_Src  = 0x223D; // Specifies the terminal of the signal to use as the synchronization pulse. The synchronization pulse resets the clock dividers and the ADCs/DACs on the device.
	static public final int DAQmx_SyncPulse_SyncTime  = 0x223E; // Indicates in seconds the delay required to reset the ADCs/DACs after the device receives the synchronization pulse.
	static public final int DAQmx_SyncPulse_MinDelayToStart  = 0x223F; // Specifies in seconds the amount of time that elapses after the master device issues the synchronization pulse before the task starts.
	static public final int DAQmx_SampTimingEngine  = 0x2A26; // Specifies which timing engine to use for the specified timing type. Refer to device documentation for information on supported timing engines.

	//********** Trigger Attributes **********
	static public final int DAQmx_StartTrig_Type  = 0x1393; // Specifies the type of trigger to use to start a task.
	static public final int DAQmx_DigEdge_StartTrig_Src  = 0x1407; // Specifies the name of a terminal where there is a digital signal to use as the source of the Start Trigger.
	static public final int DAQmx_DigEdge_StartTrig_Edge  = 0x1404; // Specifies on which edge of a digital pulse to start acquiring or generating samples.
	static public final int DAQmx_DigEdge_StartTrig_DigFltr_Enable  = 0x2223; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_DigEdge_StartTrig_DigFltr_MinPulseWidth = 0x2224; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_DigEdge_StartTrig_DigFltr_TimebaseSrc  = 0x2225; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_DigEdge_StartTrig_DigFltr_TimebaseRate = 0x2226; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_DigEdge_StartTrig_DigSync_Enable  = 0x2227; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_DigPattern_StartTrig_Src  = 0x1410; // Specifies the physical channels to use for pattern matching. The order of the physical channels determines the order of the pattern. If a port is included, the order of the physical channels within the port is in ascending order.
	static public final int DAQmx_DigPattern_StartTrig_Pattern  = 0x2186; // Specifies the digital pattern that must be met for the Start Trigger to occur.
	static public final int DAQmx_DigPattern_StartTrig_When  = 0x1411; // Specifies whether the Start Trigger occurs when the physical channels specified with Source match or differ from the digital pattern specified with Pattern.
	static public final int DAQmx_AnlgEdge_StartTrig_Src  = 0x1398; // Specifies the name of a virtual channel or terminal where there is an analog signal to use as the source of the Start Trigger.
	static public final int DAQmx_AnlgEdge_StartTrig_Slope  = 0x1397; // Specifies on which slope of the trigger signal to start acquiring or generating samples.
	static public final int DAQmx_AnlgEdge_StartTrig_Lvl  = 0x1396; // Specifies at what threshold in the units of the measurement or generation to start acquiring or generating samples. Use Slope to specify on which slope to trigger on this threshold.
	static public final int DAQmx_AnlgEdge_StartTrig_Hyst  = 0x1395; // Specifies a hysteresis level in the units of the measurement or generation. If Slope is DAQmx_Val_RisingSlope, the trigger does not deassert until the source signal passes below Level minus the hysteresis. If Slope is DAQmx_Val_FallingSlope, the trigger does not deassert until the source signal passes above Level plus the hysteresis.
	static public final int DAQmx_AnlgEdge_StartTrig_Coupling  = 0x2233; // Specifies the coupling for the source signal of the trigger if the source is a terminal rather than a virtual channel.
	static public final int DAQmx_AnlgWin_StartTrig_Src  = 0x1400; // Specifies the name of a virtual channel or terminal where there is an analog signal to use as the source of the Start Trigger.
	static public final int DAQmx_AnlgWin_StartTrig_When  = 0x1401; // Specifies whether the task starts acquiring or generating samples when the signal enters or leaves the window you specify with Bottom and Top.
	static public final int DAQmx_AnlgWin_StartTrig_Top  = 0x1403; // Specifies the upper limit of the window. Specify this value in the units of the measurement or generation.
	static public final int DAQmx_AnlgWin_StartTrig_Btm  = 0x1402; // Specifies the lower limit of the window. Specify this value in the units of the measurement or generation.
	static public final int DAQmx_AnlgWin_StartTrig_Coupling  = 0x2234; // Specifies the coupling for the source signal of the trigger if the source is a terminal rather than a virtual channel.
	static public final int DAQmx_StartTrig_Delay  = 0x1856; // Specifies an amount of time to wait after the Start Trigger is received before acquiring or generating the first sample. This value is in the units you specify with Delay Units.
	static public final int DAQmx_StartTrig_DelayUnits  = 0x18C8; // Specifies the units of Delay.
	static public final int DAQmx_StartTrig_Retriggerable  = 0x190F; // Specifies whether to enable retriggerable counter pulse generation. When you set this property to TRUE, the device generates pulses each time it receives a trigger. The device ignores a trigger if it is in the process of generating pulses.
	static public final int DAQmx_RefTrig_Type  = 0x1419; // Specifies the type of trigger to use to mark a reference point for the measurement.
	static public final int DAQmx_RefTrig_PretrigSamples  = 0x1445; // Specifies the minimum number of pretrigger samples to acquire from each channel before recognizing the reference trigger. Post-trigger samples per channel are equal to Samples Per Channel minus the number of pretrigger samples per channel.
	static public final int DAQmx_DigEdge_RefTrig_Src  = 0x1434; // Specifies the name of a terminal where there is a digital signal to use as the source of the Reference Trigger.
	static public final int DAQmx_DigEdge_RefTrig_Edge  = 0x1430; // Specifies on what edge of a digital pulse the Reference Trigger occurs.
	static public final int DAQmx_DigPattern_RefTrig_Src  = 0x1437; // Specifies the physical channels to use for pattern matching. The order of the physical channels determines the order of the pattern. If a port is included, the order of the physical channels within the port is in ascending order.
	static public final int DAQmx_DigPattern_RefTrig_Pattern  = 0x2187; // Specifies the digital pattern that must be met for the Reference Trigger to occur.
	static public final int DAQmx_DigPattern_RefTrig_When  = 0x1438; // Specifies whether the Reference Trigger occurs when the physical channels specified with Source match or differ from the digital pattern specified with Pattern.
	static public final int DAQmx_AnlgEdge_RefTrig_Src  = 0x1424; // Specifies the name of a virtual channel or terminal where there is an analog signal to use as the source of the Reference Trigger.
	static public final int DAQmx_AnlgEdge_RefTrig_Slope  = 0x1423; // Specifies on which slope of the source signal the Reference Trigger occurs.
	static public final int DAQmx_AnlgEdge_RefTrig_Lvl  = 0x1422; // Specifies in the units of the measurement the threshold at which the Reference Trigger occurs. Use Slope to specify on which slope to trigger at this threshold.
	static public final int DAQmx_AnlgEdge_RefTrig_Hyst  = 0x1421; // Specifies a hysteresis level in the units of the measurement. If Slope is DAQmx_Val_RisingSlope, the trigger does not deassert until the source signal passes below Level minus the hysteresis. If Slope is DAQmx_Val_FallingSlope, the trigger does not deassert until the source signal passes above Level plus the hysteresis.
	static public final int DAQmx_AnlgEdge_RefTrig_Coupling  = 0x2235; // Specifies the coupling for the source signal of the trigger if the source is a terminal rather than a virtual channel.
	static public final int DAQmx_AnlgWin_RefTrig_Src  = 0x1426; // Specifies the name of a virtual channel or terminal where there is an analog signal to use as the source of the Reference Trigger.
	static public final int DAQmx_AnlgWin_RefTrig_When  = 0x1427; // Specifies whether the Reference Trigger occurs when the source signal enters the window or when it leaves the window. Use Bottom and Top to specify the window.
	static public final int DAQmx_AnlgWin_RefTrig_Top  = 0x1429; // Specifies the upper limit of the window. Specify this value in the units of the measurement.
	static public final int DAQmx_AnlgWin_RefTrig_Btm  = 0x1428; // Specifies the lower limit of the window. Specify this value in the units of the measurement.
	static public final int DAQmx_AnlgWin_RefTrig_Coupling  = 0x1857; // Specifies the coupling for the source signal of the trigger if the source is a terminal rather than a virtual channel.
	static public final int DAQmx_AdvTrig_Type  = 0x1365; // Specifies the type of trigger to use to advance to the next entry in a switch scan list.
	static public final int DAQmx_DigEdge_AdvTrig_Src  = 0x1362; // Specifies the name of a terminal where there is a digital signal to use as the source of the Advance Trigger.
	static public final int DAQmx_DigEdge_AdvTrig_Edge  = 0x1360; // Specifies on which edge of a digital signal to advance to the next entry in a scan list.
	static public final int DAQmx_DigEdge_AdvTrig_DigFltr_Enable  = 0x2238; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_HshkTrig_Type  = 0x22B7; // Specifies the type of Handshake Trigger to use.
	static public final int DAQmx_Interlocked_HshkTrig_Src  = 0x22B8; // Specifies the source terminal of the Handshake Trigger.
	static public final int DAQmx_Interlocked_HshkTrig_AssertedLvl  = 0x22B9; // Specifies the asserted level of the Handshake Trigger.
	static public final int DAQmx_PauseTrig_Type  = 0x1366; // Specifies the type of trigger to use to pause a task.
	static public final int DAQmx_AnlgLvl_PauseTrig_Src  = 0x1370; // Specifies the name of a virtual channel or terminal where there is an analog signal to use as the source of the trigger.
	static public final int DAQmx_AnlgLvl_PauseTrig_When  = 0x1371; // Specifies whether the task pauses above or below the threshold you specify with Level.
	static public final int DAQmx_AnlgLvl_PauseTrig_Lvl  = 0x1369; // Specifies the threshold at which to pause the task. Specify this value in the units of the measurement or generation. Use Pause When to specify whether the task pauses above or below this threshold.
	static public final int DAQmx_AnlgLvl_PauseTrig_Hyst  = 0x1368; // Specifies a hysteresis level in the units of the measurement or generation. If Pause When is DAQmx_Val_AboveLvl, the trigger does not deassert until the source signal passes below Level minus the hysteresis. If Pause When is DAQmx_Val_BelowLvl, the trigger does not deassert until the source signal passes above Level plus the hysteresis.
	static public final int DAQmx_AnlgLvl_PauseTrig_Coupling  = 0x2236; // Specifies the coupling for the source signal of the trigger if the source is a terminal rather than a virtual channel.
	static public final int DAQmx_AnlgWin_PauseTrig_Src  = 0x1373; // Specifies the name of a virtual channel or terminal where there is an analog signal to use as the source of the trigger.
	static public final int DAQmx_AnlgWin_PauseTrig_When  = 0x1374; // Specifies whether the task pauses while the trigger signal is inside or outside the window you specify with Bottom and Top.
	static public final int DAQmx_AnlgWin_PauseTrig_Top  = 0x1376; // Specifies the upper limit of the window. Specify this value in the units of the measurement or generation.
	static public final int DAQmx_AnlgWin_PauseTrig_Btm  = 0x1375; // Specifies the lower limit of the window. Specify this value in the units of the measurement or generation.
	static public final int DAQmx_AnlgWin_PauseTrig_Coupling  = 0x2237; // Specifies the coupling for the source signal of the trigger if the source is a terminal rather than a virtual channel.
	static public final int DAQmx_DigLvl_PauseTrig_Src  = 0x1379; // Specifies the name of a terminal where there is a digital signal to use as the source of the Pause Trigger.
	static public final int DAQmx_DigLvl_PauseTrig_When  = 0x1380; // Specifies whether the task pauses while the signal is high or low.
	static public final int DAQmx_DigLvl_PauseTrig_DigFltr_Enable  = 0x2228; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_DigLvl_PauseTrig_DigFltr_MinPulseWidth = 0x2229; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_DigLvl_PauseTrig_DigFltr_TimebaseSrc  = 0x222A; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_DigLvl_PauseTrig_DigFltr_TimebaseRate  = 0x222B; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_DigLvl_PauseTrig_DigSync_Enable  = 0x222C; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.
	static public final int DAQmx_DigPattern_PauseTrig_Src  = 0x216F; // Specifies the physical channels to use for pattern matching. The order of the physical channels determines the order of the pattern. If a port is included, the lines within the port are in ascending order.
	static public final int DAQmx_DigPattern_PauseTrig_Pattern  = 0x2188; // Specifies the digital pattern that must be met for the Pause Trigger to occur.
	static public final int DAQmx_DigPattern_PauseTrig_When  = 0x2170; // Specifies if the Pause Trigger occurs when the physical channels specified with Source match or differ from the digital pattern specified with Pattern.
	static public final int DAQmx_ArmStartTrig_Type  = 0x1414; // Specifies the type of trigger to use to arm the task for a Start Trigger. If you configure an Arm Start Trigger, the task does not respond to a Start Trigger until the device receives the Arm Start Trigger.
	static public final int DAQmx_DigEdge_ArmStartTrig_Src  = 0x1417; // Specifies the name of a terminal where there is a digital signal to use as the source of the Arm Start Trigger.
	static public final int DAQmx_DigEdge_ArmStartTrig_Edge  = 0x1415; // Specifies on which edge of a digital signal to arm the task for a Start Trigger.
	static public final int DAQmx_DigEdge_ArmStartTrig_DigFltr_Enable  = 0x222D; // Specifies whether to apply the pulse width filter to the signal.
	static public final int DAQmx_DigEdge_ArmStartTrig_DigFltr_MinPulseWidth = 0x222E; // Specifies in seconds the minimum pulse width the filter recognizes.
	static public final int DAQmx_DigEdge_ArmStartTrig_DigFltr_TimebaseSrc = 0x222F; // Specifies the input terminal of the signal to use as the timebase of the pulse width filter.
	static public final int DAQmx_DigEdge_ArmStartTrig_DigFltr_TimebaseRate = 0x2230; // Specifies in hertz the rate of the pulse width filter timebase. NI-DAQmx uses this value to compute settings for the filter.
	static public final int DAQmx_DigEdge_ArmStartTrig_DigSync_Enable  = 0x2231; // Specifies whether to synchronize recognition of transitions in the signal to the internal timebase of the device.

	//********** Watchdog Attributes **********
	static public final int DAQmx_Watchdog_Timeout  = 0x21A9; // Specifies in seconds the amount of time until the watchdog timer expires. A value of -1 means the internal timer never expires. Set this input to -1 if you use an Expiration Trigger to expire the watchdog task.
	static public final int DAQmx_WatchdogExpirTrig_Type  = 0x21A3; // Specifies the type of trigger to use to expire a watchdog task.
	static public final int DAQmx_DigEdge_WatchdogExpirTrig_Src  = 0x21A4; // Specifies the name of a terminal where a digital signal exists to use as the source of the Expiration Trigger.
	static public final int DAQmx_DigEdge_WatchdogExpirTrig_Edge  = 0x21A5; // Specifies on which edge of a digital signal to expire the watchdog task.
	static public final int DAQmx_Watchdog_DO_ExpirState  = 0x21A7; // Specifies the state to which to set the digital physical channels when the watchdog task expires. You cannot modify the expiration state of dedicated digital input physical channels.
	static public final int DAQmx_Watchdog_HasExpired  = 0x21A8; // Indicates if the watchdog timer expired. You can read this property only while the task is running.

	//********** Write Attributes **********
	static public final int DAQmx_Write_RelativeTo  = 0x190C; // Specifies the point in the buffer at which to write data. If you also specify an offset with Offset, the write operation begins at that offset relative to this point you select with this property.
	static public final int DAQmx_Write_Offset  = 0x190D; // Specifies in samples per channel an offset at which a write operation begins. This offset is relative to the location you specify with Relative To.
	static public final int DAQmx_Write_RegenMode  = 0x1453; // Specifies whether to allow NI-DAQmx to generate the same data multiple times.
	static public final int DAQmx_Write_CurrWritePos  = 0x1458; // Indicates the position in the buffer of the next sample to generate. This value is identical for all channels in the task.
	static public final int DAQmx_Write_OvercurrentChansExist  = 0x29E8; // Indicates if the device(s) detected an overcurrent condition for any channel in the task. Reading this property clears the overcurrent status for all channels in the task. You must read this property before you read Overcurrent Channels. Otherwise, you will receive an error.
	static public final int DAQmx_Write_OvercurrentChans  = 0x29E9; // Indicates the names of any virtual channels in the task for which an overcurrent condition has been detected. You must read Overcurrent Channels Exist before you read this property. Otherwise, you will receive an error.
	static public final int DAQmx_Write_OvertemperatureChansExist  = 0x2A84; // Indicates if the device(s) detected a temperature above their safe operating level. If a device exceeds this temperature, the device shuts off its output channels until the temperature returns to a safe level.
	static public final int DAQmx_Write_OpenCurrentLoopChansExist  = 0x29EA; // Indicates if the device(s) detected an open current loop for any channel in the task. Reading this property clears the open current loop status for all channels in the task. You must read this property before you read Open Current Loop Channels. Otherwise, you will receive an error.
	static public final int DAQmx_Write_OpenCurrentLoopChans  = 0x29EB; // Indicates the names of any virtual channels in the task for which the device(s) detected an open current loop. You must read Open Current Loop Channels Exist before you read this property. Otherwise, you will receive an error.
	static public final int DAQmx_Write_PowerSupplyFaultChansExist  = 0x29EC; // Indicates if the device(s) detected a power supply fault for any channel in the task. Reading this property clears the power supply fault status for all channels in the task. You must read this property before you read Power Supply Fault Channels. Otherwise, you will receive an error.
	static public final int DAQmx_Write_PowerSupplyFaultChans  = 0x29ED; // Indicates the names of any virtual channels in the task that have a power supply fault. You must read Power Supply Fault Channels Exist before you read this property. Otherwise, you will receive an error.
	static public final int DAQmx_Write_SpaceAvail  = 0x1460; // Indicates in samples per channel the amount of available space in the buffer.
	static public final int DAQmx_Write_TotalSampPerChanGenerated  = 0x192B; // Indicates the total number of samples generated by each channel in the task. This value is identical for all channels in the task.
	static public final int DAQmx_Write_RawDataWidth  = 0x217D; // Indicates in bytes the required size of a raw sample to write to the task.
	static public final int DAQmx_Write_NumChans  = 0x217E; // Indicates the number of channels that an NI-DAQmx Write function writes to the task. This value is the number of channels in the task.
	static public final int DAQmx_Write_WaitMode  = 0x22B1; // Specifies how an NI-DAQmx Write function waits for space to become available in the buffer.
	static public final int DAQmx_Write_SleepTime  = 0x22B2; // Specifies in seconds the amount of time to sleep after checking for available buffer space if Wait Mode is DAQmx_Val_Sleep.
	static public final int DAQmx_Write_NextWriteIsLast  = 0x296C; // Specifies that the next samples written are the last samples you want to generate. Use this property when performing continuous generation to prevent underflow errors after writing the last sample. Regeneration Mode must be DAQmx_Val_DoNotAllowRegen to use this property.
	static public final int DAQmx_Write_DigitalLines_BytesPerChan  = 0x217F; // Indicates the number of bytes expected per channel in a sample for line-based writes. If a channel has fewer lines than this number, NI-DAQmx ignores the extra bytes.

	//********** Physical Channel Attributes **********
	static public final int DAQmx_PhysicalChan_AI_TermCfgs  = 0x2342; // Indicates the list of terminal configurations supported by the channel.
	static public final int DAQmx_PhysicalChan_AO_TermCfgs  = 0x29A3; // Indicates the list of terminal configurations supported by the channel.
	static public final int DAQmx_PhysicalChan_AO_ManualControlEnable  = 0x2A1E; // Specifies if you can control the physical channel externally via a manual control located on the device. You cannot simultaneously control a channel manually and with NI-DAQmx.
	static public final int DAQmx_PhysicalChan_AO_ManualControlAmplitude = 0x2A1F; // Indicates the current value of the front panel amplitude control for the physical channel in volts.
	static public final int DAQmx_PhysicalChan_AO_ManualControlFreq  = 0x2A20; // Indicates the current value of the front panel frequency control for the physical channel in hertz.
	static public final int DAQmx_PhysicalChan_DI_PortWidth  = 0x29A4; // Indicates in bits the width of digital input port.
	static public final int DAQmx_PhysicalChan_DI_SampClkSupported  = 0x29A5; // Indicates if the sample clock timing type is supported for the digital input physical channel.
	static public final int DAQmx_PhysicalChan_DI_ChangeDetectSupported  = 0x29A6; // Indicates if the change detection timing type is supported for the digital input physical channel.
	static public final int DAQmx_PhysicalChan_DO_PortWidth  = 0x29A7; // Indicates in bits the width of digital output port.
	static public final int DAQmx_PhysicalChan_DO_SampClkSupported  = 0x29A8; // Indicates if the sample clock timing type is supported for the digital output physical channel.
	static public final int DAQmx_PhysicalChan_TEDS_MfgID  = 0x21DA; // Indicates the manufacturer ID of the sensor.
	static public final int DAQmx_PhysicalChan_TEDS_ModelNum  = 0x21DB; // Indicates the model number of the sensor.
	static public final int DAQmx_PhysicalChan_TEDS_SerialNum  = 0x21DC; // Indicates the serial number of the sensor.
	static public final int DAQmx_PhysicalChan_TEDS_VersionNum  = 0x21DD; // Indicates the version number of the sensor.
	static public final int DAQmx_PhysicalChan_TEDS_VersionLetter  = 0x21DE; // Indicates the version letter of the sensor.
	static public final int DAQmx_PhysicalChan_TEDS_BitStream  = 0x21DF; // Indicates the TEDS binary bitstream without checksums.
	static public final int DAQmx_PhysicalChan_TEDS_TemplateIDs  = 0x228F; // Indicates the IDs of the templates in the bitstream in BitStream.

	//********** Persisted Task Attributes **********
	static public final int DAQmx_PersistedTask_Author  = 0x22CC; // Indicates the author of the task.
	static public final int DAQmx_PersistedTask_AllowInteractiveEditing  = 0x22CD; // Indicates whether the task can be edited in the DAQ Assistant.
	static public final int DAQmx_PersistedTask_AllowInteractiveDeletion = 0x22CE; // Indicates whether the task can be deleted through MAX.

	//********** Persisted Channel Attributes **********
	static public final int DAQmx_PersistedChan_Author  = 0x22D0; // Indicates the author of the global channel.
	static public final int DAQmx_PersistedChan_AllowInteractiveEditing  = 0x22D1; // Indicates whether the global channel can be edited in the DAQ Assistant.
	static public final int DAQmx_PersistedChan_AllowInteractiveDeletion = 0x22D2; // Indicates whether the global channel can be deleted through MAX.

	//********** Persisted Scale Attributes **********
	static public final int DAQmx_PersistedScale_Author  = 0x22D4; // Indicates the author of the custom scale.
	static public final int DAQmx_PersistedScale_AllowInteractiveEditing = 0x22D5; // Indicates whether the custom scale can be edited in the DAQ Assistant.
	static public final int DAQmx_PersistedScale_AllowInteractiveDeletion = 0x22D6; // Indicates whether the custom scale can be deleted through MAX.


	// For backwards compatibility, the DAQmx_ReadWaitMode has to be defined because this was the original spelling
	// that has been later on corrected.
//	static public final int DAQmx_ReadWaitMode=	DAQmx_Read_WaitMode

	/******************************************************************************
	 *** NI-DAQmx Values **********************************************************
	 ******************************************************************************/

	/******************************************************/
	/*** Non-Attribute Function Parameter Values ***/
	/******************************************************/

	//*** Values for the Mode parameter of DAQmxTaskControl ***
	static public final int DAQmx_Val_Task_Start = 0 ; // Start
	static public final int DAQmx_Val_Task_Stop = 1 ; // Stop
	static public final int DAQmx_Val_Task_Verify = 2 ; // Verify
	static public final int DAQmx_Val_Task_Commit = 3 ; // Commit
	static public final int DAQmx_Val_Task_Reserve = 4 ; // Reserve
	static public final int DAQmx_Val_Task_Unreserve = 5 ; // Unreserve
	static public final int DAQmx_Val_Task_Abort = 6 ; // Abort

	//*** Values for the Options parameter of the event registration functions
	static public final int DAQmx_Val_SynchronousEventCallbacks = (1<<0) ; // Synchronous callbacks

	//*** Values for the everyNsamplesEventType parameter of DAQmxRegisterEveryNSamplesEvent ***
	static public final int DAQmx_Val_Acquired_Into_Buffer = 1 ; // Acquired Into Buffer
	static public final int DAQmx_Val_Transferred_From_Buffer = 2 ; // Transferred From Buffer


	//*** Values for the Action parameter of DAQmxControlWatchdogTask ***
	static public final int DAQmx_Val_ResetTimer = 0 ; // Reset Timer
	static public final int DAQmx_Val_ClearExpiration = 1 ; // Clear Expiration

	//*** Values for the Line Grouping parameter of DAQmxCreateDIChan and DAQmxCreateDOChan ***
	static public final int DAQmx_Val_ChanPerLine = 0 ; // One Channel For Each Line
	static public final int DAQmx_Val_ChanForAllLines = 1 ; // One Channel For All Lines

	//*** Values for the Fill Mode parameter of DAQmxReadAnalogF64, DAQmxReadBinaryI16, DAQmxReadBinaryU16, DAQmxReadBinaryI32, DAQmxReadBinaryU32,
//	 DAQmxReadDigitalU8, DAQmxReadDigitalU32, DAQmxReadDigitalLines ***
	//*** Values for the Data Layout parameter of DAQmxWriteAnalogF64, DAQmxWriteBinaryI16, DAQmxWriteDigitalU8, DAQmxWriteDigitalU32, DAQmxWriteDigitalLines ***
	static public final int DAQmx_Val_GroupByChannel = 0 ; // Group by Channel
	static public final int DAQmx_Val_GroupByScanNumber = 1 ; // Group by Scan Number

	//*** Values for the Signal Modifiers parameter of DAQmxConnectTerms ***/
	static public final int DAQmx_Val_DoNotInvertPolarity = 0 ; // Do not invert polarity
	static public final int DAQmx_Val_InvertPolarity = 1 ; // Invert polarity

	//*** Values for the Action paramter of DAQmxCloseExtCal ***
	static public final int DAQmx_Val_Action_Commit = 0 ; // Commit
	static public final int DAQmx_Val_Action_Cancel = 1 ; // Cancel

	//*** Values for the Trigger ID parameter of DAQmxSendSoftwareTrigger ***
	static public final int DAQmx_Val_AdvanceTrigger = 12488; // Advance Trigger

	//*** Value set for the ActiveEdge parameter of DAQmxCfgSampClkTiming and DAQmxCfgPipelinedSampClkTiming ***
	static public final int DAQmx_Val_Rising = 10280; // Rising
	static public final int DAQmx_Val_Falling = 10171; // Falling

	//*** Value set SwitchPathType ***
	//*** Value set for the output Path Status parameter of DAQmxSwitchFindPath ***
	static public final int DAQmx_Val_PathStatus_Available = 10431; // Path Available
	static public final int DAQmx_Val_PathStatus_AlreadyExists = 10432; // Path Already Exists
	static public final int DAQmx_Val_PathStatus_Unsupported = 10433; // Path Unsupported
	static public final int DAQmx_Val_PathStatus_ChannelInUse = 10434; // Channel In Use
	static public final int DAQmx_Val_PathStatus_SourceChannelConflict = 10435; // Channel Source Conflict
	static public final int DAQmx_Val_PathStatus_ChannelReservedForRouting = 10436; // Channel Reserved for Routing

	//*** Value set for the Units parameter of DAQmxCreateAIThrmcplChan, DAQmxCreateAIRTDChan, DAQmxCreateAIThrmstrChanIex, DAQmxCreateAIThrmstrChanVex and DAQmxCreateAITempBuiltInSensorChan ***
	static public final int DAQmx_Val_DegC = 10143; // Deg C
	static public final int DAQmx_Val_DegF = 10144; // Deg F
	static public final int DAQmx_Val_Kelvins = 10325; // Kelvins
	static public final int DAQmx_Val_DegR = 10145; // Deg R

	//*** Value set for the state parameter of DAQmxSetDigitalPowerUpStates ***
	static public final int DAQmx_Val_High = 10192; // High
	static public final int DAQmx_Val_Low = 10214; // Low
	static public final int DAQmx_Val_Tristate = 10310; // Tristate

	//*** Value set for the channelType parameter of DAQmxSetAnalogPowerUpStates ***
	static public final int DAQmx_Val_ChannelVoltage = 0 ; // Voltage Channel
	static public final int DAQmx_Val_ChannelCurrent = 1 ; // Current Channel

	//*** Value set RelayPos ***
	//*** Value set for the state parameter of DAQmxSwitchGetSingleRelayPos and DAQmxSwitchGetMultiRelayPos ***
	static public final int DAQmx_Val_Open = 10437; // Open
	static public final int DAQmx_Val_Closed = 10438; // Closed


	//*** Value set for the inputCalSource parameter of DAQmxAdjust1540Cal ***
	static public final int DAQmx_Val_Loopback0 = 0 ; // Loopback 0 degree shift
	static public final int DAQmx_Val_Loopback180 = 1 ; // Loopback 180 degree shift
	static public final int DAQmx_Val_Ground = 2 ; // Ground


	//*** Value for the Terminal Config parameter of DAQmxCreateAIVoltageChan, DAQmxCreateAICurrentChan and DAQmxCreateAIVoltageChanWithExcit ***
	static public final int DAQmx_Val_Cfg_Default = -1; // Default
	//*** Value for the Shunt Resistor Location parameter of DAQmxCreateAICurrentChan ***
	static public final int DAQmx_Val_Default = -1; // Default

	//*** Value for the Timeout parameter of DAQmxWaitUntilTaskDone
	static public final double DAQmx_Val_WaitInfinitely = -1.0;

	//*** Value for the Number of Samples per Channel parameter of DAQmxReadAnalogF64, DAQmxReadBinaryI16, DAQmxReadBinaryU16,
//	 DAQmxReadBinaryI32, DAQmxReadBinaryU32, DAQmxReadDigitalU8, DAQmxReadDigitalU32,
//	 DAQmxReadDigitalLines, DAQmxReadCounterF64, DAQmxReadCounterU32 and DAQmxReadRaw ***
	static public final int DAQmx_Val_Auto = -1;

	// Value set for the Options parameter of DAQmxSaveTask, DAQmxSaveGlobalChan and DAQmxSaveScale
	static public final int DAQmx_Val_Save_Overwrite = (1<<0);
	static public final int DAQmx_Val_Save_AllowInteractiveEditing = (1<<1);
	static public final int DAQmx_Val_Save_AllowInteractiveDeletion = (1<<2);

	//*** Values for the Trigger Usage parameter - set of trigger types a device may support
	//*** Values for TriggerUsageTypeBits
	static public final int DAQmx_Val_Bit_TriggerUsageTypes_Advance = (1<<0); // Device supports advance triggers
	static public final int DAQmx_Val_Bit_TriggerUsageTypes_Pause = (1<<1); // Device supports pause triggers
	static public final int DAQmx_Val_Bit_TriggerUsageTypes_Reference = (1<<2); // Device supports reference triggers
	static public final int DAQmx_Val_Bit_TriggerUsageTypes_Start = (1<<3); // Device supports start triggers
	static public final int DAQmx_Val_Bit_TriggerUsageTypes_Handshake = (1<<4); // Device supports handshake triggers
	static public final int DAQmx_Val_Bit_TriggerUsageTypes_ArmStart = (1<<5); // Device supports arm start triggers

	//*** Values for the Coupling Types parameter - set of coupling types a device may support
	//*** Values for CouplingTypeBits
	static public final int DAQmx_Val_Bit_CouplingTypes_AC = (1<<0); // Device supports AC coupling
	static public final int DAQmx_Val_Bit_CouplingTypes_DC = (1<<1); // Device supports DC coupling
	static public final int DAQmx_Val_Bit_CouplingTypes_Ground = (1<<2); // Device supports ground coupling
	static public final int DAQmx_Val_Bit_CouplingTypes_HFReject = (1<<3); // Device supports High Frequency Reject coupling
	static public final int DAQmx_Val_Bit_CouplingTypes_LFReject = (1<<4); // Device supports Low Frequency Reject coupling
	static public final int DAQmx_Val_Bit_CouplingTypes_NoiseReject = (1<<5); // Device supports Noise Reject coupling

	//*** Values for DAQmx_PhysicalChan_AI_TermCfgs and DAQmx_PhysicalChan_AO_TermCfgs
	//*** Value set TerminalConfigurationBits ***
//	static public final int DAQmx_Val_Bit_TermCfg_RSE = (1<<0); // RSE terminal configuration
//	static public final int DAQmx_Val_Bit_TermCfg_NRSE = (1<<1); // NRSE terminal configuration
//	static public final int DAQmx_Val_Bit_TermCfg_Diff = (1<<2); // Differential terminal configuration
//	static public final int DAQmx_Val_Bit_TermCfg_PseudoDIFF = (1<<3); // Pseudodifferential terminal configuration


	/******************************************************/
	/*** Attribute Values ***/
	/******************************************************/

	//*** Values for DAQmx_AI_ACExcit_WireMode ***
	//*** Value set ACExcitWireMode ***
	static public final int DAQmx_Val_4Wire = 4; // 4-Wire
	static public final int DAQmx_Val_5Wire = 5; // 5-Wire

	//*** Values for DAQmx_AI_ADCTimingMode ***
	//*** Value set ADCTimingMode ***
	static public final int DAQmx_Val_HighResolution = 10195; // High Resolution
	static public final int DAQmx_Val_HighSpeed = 14712; // High Speed
	static public final int DAQmx_Val_Best50HzRejection = 14713; // Best 50 Hz Rejection
	static public final int DAQmx_Val_Best60HzRejection = 14714; // Best 60 Hz Rejection

	//*** Values for DAQmx_AI_MeasType ***
	//*** Value set AIMeasurementType ***
	static public final int DAQmx_Val_Voltage = 10322; // Voltage
	static public final int DAQmx_Val_VoltageRMS = 10350; // Voltage RMS
	static public final int DAQmx_Val_Current = 10134; // Current
	static public final int DAQmx_Val_CurrentRMS = 10351; // Current RMS
	static public final int DAQmx_Val_Voltage_CustomWithExcitation = 10323; // More:Voltage:Custom with Excitation
	static public final int DAQmx_Val_Freq_Voltage = 10181; // Frequency
	static public final int DAQmx_Val_Resistance = 10278; // Resistance
	static public final int DAQmx_Val_Temp_TC = 10303; // Temperature:Thermocouple
	static public final int DAQmx_Val_Temp_Thrmstr = 10302; // Temperature:Thermistor
	static public final int DAQmx_Val_Temp_RTD = 10301; // Temperature:RTD
	static public final int DAQmx_Val_Temp_BuiltInSensor = 10311; // Temperature:Built-in Sensor
	static public final int DAQmx_Val_Strain_Gage = 10300; // Strain Gage
	static public final int DAQmx_Val_Position_LVDT = 10352; // Position:LVDT
	static public final int DAQmx_Val_Position_RVDT = 10353; // Position:RVDT
	static public final int DAQmx_Val_Accelerometer = 10356; // Accelerometer
	static public final int DAQmx_Val_SoundPressure_Microphone = 10354; // Sound Pressure:Microphone
	static public final int DAQmx_Val_TEDS_Sensor = 12531; // TEDS Sensor

	//*** Values for DAQmx_AO_IdleOutputBehavior ***
	//*** Value set AOIdleOutputBehavior ***
	static public final int DAQmx_Val_ZeroVolts = 12526; // Zero Volts
	static public final int DAQmx_Val_HighImpedance = 12527; // High Impedance
	static public final int DAQmx_Val_MaintainExistingValue = 12528; // Maintain Existing Value

	//*** Values for DAQmx_AO_OutputType ***
	//*** Value set AOOutputChannelType ***
//	static public final int DAQmx_Val_Voltage = 10322; // Voltage
//	static public final int DAQmx_Val_Current = 10134; // Current
	static public final int DAQmx_Val_FuncGen = 14750; // Function Generation

	//*** Values for DAQmx_AI_Accel_SensitivityUnits ***
	//*** Value set AccelSensitivityUnits1 ***
	static public final int DAQmx_Val_mVoltsPerG = 12509; // mVolts/g
	static public final int DAQmx_Val_VoltsPerG = 12510; // Volts/g

	//*** Values for DAQmx_AI_Accel_Units ***
	//*** Value set AccelUnits2 ***
	static public final int DAQmx_Val_AccelUnit_g = 10186; // g
	static public final int DAQmx_Val_MetersPerSecondSquared = 12470; // m/s^2
	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_SampQuant_SampMode ***
	//*** Value set AcquisitionType ***
	static public final int DAQmx_Val_FiniteSamps = 10178; // Finite Samples
	static public final int DAQmx_Val_ContSamps = 10123; // Continuous Samples
	static public final int DAQmx_Val_HWTimedSinglePoint = 12522; // Hardware Timed Single Point

	//*** Values for DAQmx_AnlgLvl_PauseTrig_When ***
	//*** Value set ActiveLevel ***
	static public final int DAQmx_Val_AboveLvl = 10093; // Above Level
	static public final int DAQmx_Val_BelowLvl = 10107; // Below Level

	//*** Values for DAQmx_AI_RVDT_Units ***
	//*** Value set AngleUnits1 ***
	static public final int DAQmx_Val_Degrees = 10146; // Degrees
	static public final int DAQmx_Val_Radians = 10273; // Radians
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_CI_AngEncoder_Units ***
	//*** Value set AngleUnits2 ***
//	static public final int DAQmx_Val_Degrees = 10146; // Degrees
//	static public final int DAQmx_Val_Radians = 10273; // Radians
	static public final int DAQmx_Val_Ticks = 10304; // Ticks
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_AI_AutoZeroMode ***
	//*** Value set AutoZeroType1 ***
	static public final int DAQmx_Val_None = 10230; // None
	static public final int DAQmx_Val_Once = 10244; // Once
	static public final int DAQmx_Val_EverySample = 10164; // Every Sample

	//*** Values for DAQmx_SwitchScan_BreakMode ***
	//*** Value set BreakMode ***
	static public final int DAQmx_Val_NoAction = 10227; // No Action
	static public final int DAQmx_Val_BreakBeforeMake = 10110; // Break Before Make

	//*** Values for DAQmx_AI_Bridge_Cfg ***
	//*** Value set BridgeConfiguration1 ***
	static public final int DAQmx_Val_FullBridge = 10182; // Full Bridge
	static public final int DAQmx_Val_HalfBridge = 10187; // Half Bridge
	static public final int DAQmx_Val_QuarterBridge = 10270; // Quarter Bridge
	static public final int DAQmx_Val_NoBridge = 10228; // No Bridge

	//*** Values for DAQmx_Dev_BusType ***
	//*** Value set BusType ***
	static public final int DAQmx_Val_PCI = 12582; // PCI
	static public final int DAQmx_Val_PCIe = 13612; // PCIe
	static public final int DAQmx_Val_PXI = 12583; // PXI
	static public final int DAQmx_Val_PXIe = 14706; // PXIe
	static public final int DAQmx_Val_SCXI = 12584; // SCXI
	static public final int DAQmx_Val_SCC = 14707; // SCC
	static public final int DAQmx_Val_PCCard = 12585; // PCCard
	static public final int DAQmx_Val_USB = 12586; // USB
	static public final int DAQmx_Val_CompactDAQ = 14637; // CompactDAQ
	static public final int DAQmx_Val_TCPIP = 14828; // TCP/IP
	static public final int DAQmx_Val_Unknown = 12588; // Unknown

	//*** Values for DAQmx_CI_MeasType ***
	//*** Value set CIMeasurementType ***
	static public final int DAQmx_Val_CountEdges = 10125; // Count Edges
	static public final int DAQmx_Val_Freq = 10179; // Frequency
	static public final int DAQmx_Val_Period = 10256; // Period
	static public final int DAQmx_Val_PulseWidth = 10359; // Pulse Width
	static public final int DAQmx_Val_SemiPeriod = 10289; // Semi Period
	static public final int DAQmx_Val_Position_AngEncoder = 10360; // Position:Angular Encoder
	static public final int DAQmx_Val_Position_LinEncoder = 10361; // Position:Linear Encoder
	static public final int DAQmx_Val_TwoEdgeSep = 10267; // Two Edge Separation
	static public final int DAQmx_Val_GPS_Timestamp = 10362; // GPS Timestamp

	//*** Values for DAQmx_AI_Thrmcpl_CJCSrc ***
	//*** Value set CJCSource1 ***
	static public final int DAQmx_Val_BuiltIn = 10200; // Built-In
	static public final int DAQmx_Val_ConstVal = 10116; // Constant Value
	static public final int DAQmx_Val_Chan = 10113; // Channel

	//*** Values for DAQmx_CO_OutputType ***
	//*** Value set COOutputType ***
	static public final int DAQmx_Val_Pulse_Time = 10269; // Pulse:Time
	static public final int DAQmx_Val_Pulse_Freq = 10119; // Pulse:Frequency
	static public final int DAQmx_Val_Pulse_Ticks = 10268; // Pulse:Ticks

	//*** Values for DAQmx_ChanType ***
	//*** Value set ChannelType ***
	static public final int DAQmx_Val_AI = 10100; // Analog Input
	static public final int DAQmx_Val_AO = 10102; // Analog Output
	static public final int DAQmx_Val_DI = 10151; // Digital Input
	static public final int DAQmx_Val_DO = 10153; // Digital Output
	static public final int DAQmx_Val_CI = 10131; // Counter Input
	static public final int DAQmx_Val_CO = 10132; // Counter Output

	//*** Values for DAQmx_CO_ConstrainedGenMode ***
	//*** Value set ConstrainedGenMode ***
	static public final int DAQmx_Val_Unconstrained = 14708; // Unconstrained
	static public final int DAQmx_Val_FixedHighFreq = 14709; // Fixed High Frequency
	static public final int DAQmx_Val_FixedLowFreq = 14710; // Fixed Low Frequency
	static public final int DAQmx_Val_Fixed50PercentDutyCycle = 14711; // Fixed 50 Percent Duty Cycle

	//*** Values for DAQmx_CI_CountEdges_Dir ***
	//*** Value set CountDirection1 ***
	static public final int DAQmx_Val_CountUp = 10128; // Count Up
	static public final int DAQmx_Val_CountDown = 10124; // Count Down
	static public final int DAQmx_Val_ExtControlled = 10326; // Externally Controlled

	//*** Values for DAQmx_CI_Freq_MeasMeth ***
	//*** Values for DAQmx_CI_Period_MeasMeth ***
	//*** Value set CounterFrequencyMethod ***
	static public final int DAQmx_Val_LowFreq1Ctr = 10105; // Low Frequency with 1 Counter
	static public final int DAQmx_Val_HighFreq2Ctr = 10157; // High Frequency with 2 Counters
	static public final int DAQmx_Val_LargeRng2Ctr = 10205; // Large Range with 2 Counters

	//*** Values for DAQmx_AI_Coupling ***
	//*** Value set Coupling1 ***
	static public final int DAQmx_Val_AC = 10045; // AC
	static public final int DAQmx_Val_DC = 10050; // DC
	static public final int DAQmx_Val_GND = 10066; // GND

	//*** Values for DAQmx_AnlgEdge_StartTrig_Coupling ***
	//*** Values for DAQmx_AnlgWin_StartTrig_Coupling ***
	//*** Values for DAQmx_AnlgEdge_RefTrig_Coupling ***
	//*** Values for DAQmx_AnlgWin_RefTrig_Coupling ***
	//*** Values for DAQmx_AnlgLvl_PauseTrig_Coupling ***
	//*** Values for DAQmx_AnlgWin_PauseTrig_Coupling ***
	//*** Value set Coupling2 ***
//	static public final int DAQmx_Val_AC = 10045; // AC
//	static public final int DAQmx_Val_DC = 10050; // DC

	//*** Values for DAQmx_AI_CurrentShunt_Loc ***
	//*** Value set CurrentShuntResistorLocation1 ***
	static public final int DAQmx_Val_Internal = 10200; // Internal
	static public final int DAQmx_Val_External = 10167; // External

	//*** Values for DAQmx_AI_Current_Units ***
	//*** Values for DAQmx_AI_Current_ACRMS_Units ***
	//*** Values for DAQmx_AO_Current_Units ***
	//*** Value set CurrentUnits1 ***
	static public final int DAQmx_Val_Amps = 10342; // Amps
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale
	static public final int DAQmx_Val_FromTEDS = 12516; // From TEDS

	//*** Value set CurrentUnits2 ***
//	static public final int DAQmx_Val_Amps = 10342; // Amps
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_AI_RawSampJustification ***
	//*** Value set DataJustification1 ***
	static public final int DAQmx_Val_RightJustified = 10279; // Right-Justified
	static public final int DAQmx_Val_LeftJustified = 10209; // Left-Justified

	//*** Values for DAQmx_AI_DataXferMech ***
	//*** Values for DAQmx_AO_DataXferMech ***
	//*** Values for DAQmx_DI_DataXferMech ***
	//*** Values for DAQmx_DO_DataXferMech ***
	//*** Values for DAQmx_CI_DataXferMech ***
	//*** Value set DataTransferMechanism ***
	static public final int DAQmx_Val_DMA = 10054; // DMA
	static public final int DAQmx_Val_Interrupts = 10204; // Interrupts
	static public final int DAQmx_Val_ProgrammedIO = 10264; // Programmed I/O
	static public final int DAQmx_Val_USBbulk = 12590; // USB Bulk

	//*** Values for DAQmx_Exported_RdyForXferEvent_DeassertCond ***
	//*** Value set DeassertCondition ***
	static public final int DAQmx_Val_OnbrdMemMoreThanHalfFull = 10237; // Onboard Memory More than Half Full
	static public final int DAQmx_Val_OnbrdMemFull = 10236; // Onboard Memory Full
	static public final int DAQmx_Val_OnbrdMemCustomThreshold = 12577; // Onboard Memory Custom Threshold

	//*** Values for DAQmx_DO_OutputDriveType ***
	//*** Value set DigitalDriveType ***
	static public final int DAQmx_Val_ActiveDrive = 12573; // Active Drive
	static public final int DAQmx_Val_OpenCollector = 12574; // Open Collector

	//*** Values for DAQmx_DO_LineStates_StartState ***
	//*** Values for DAQmx_DO_LineStates_PausedState ***
	//*** Values for DAQmx_DO_LineStates_DoneState ***
	//*** Values for DAQmx_Watchdog_DO_ExpirState ***
	//*** Value set DigitalLineState ***
//	static public final int DAQmx_Val_High = 10192; // High
//	static public final int DAQmx_Val_Low = 10214; // Low
//	static public final int DAQmx_Val_Tristate = 10310; // Tristate
	static public final int DAQmx_Val_NoChange = 10160; // No Change

	//*** Values for DAQmx_DigPattern_StartTrig_When ***
	//*** Values for DAQmx_DigPattern_RefTrig_When ***
	//*** Values for DAQmx_DigPattern_PauseTrig_When ***
	//*** Value set DigitalPatternCondition1 ***
	static public final int DAQmx_Val_PatternMatches = 10254; // Pattern Matches
	static public final int DAQmx_Val_PatternDoesNotMatch = 10253; // Pattern Does Not Match

	//*** Values for DAQmx_StartTrig_DelayUnits ***
	//*** Value set DigitalWidthUnits1 ***
	static public final int DAQmx_Val_SampClkPeriods = 10286; // Sample Clock Periods
	static public final int DAQmx_Val_Seconds = 10364; // Seconds
//	static public final int DAQmx_Val_Ticks = 10304; // Ticks

	//*** Values for DAQmx_DelayFromSampClk_DelayUnits ***
	//*** Value set DigitalWidthUnits2 ***
//	static public final int DAQmx_Val_Seconds = 10364; // Seconds
//	static public final int DAQmx_Val_Ticks = 10304; // Ticks

	//*** Values for DAQmx_Exported_AdvTrig_Pulse_WidthUnits ***
	//*** Value set DigitalWidthUnits3 ***
//	static public final int DAQmx_Val_Seconds = 10364; // Seconds

	//*** Values for DAQmx_CI_Freq_StartingEdge ***
	//*** Values for DAQmx_CI_Period_StartingEdge ***
	//*** Values for DAQmx_CI_CountEdges_ActiveEdge ***
	//*** Values for DAQmx_CI_PulseWidth_StartingEdge ***
	//*** Values for DAQmx_CI_TwoEdgeSep_FirstEdge ***
	//*** Values for DAQmx_CI_TwoEdgeSep_SecondEdge ***
	//*** Values for DAQmx_CI_SemiPeriod_StartingEdge ***
	//*** Values for DAQmx_CI_CtrTimebaseActiveEdge ***
	//*** Values for DAQmx_CO_CtrTimebaseActiveEdge ***
	//*** Values for DAQmx_SampClk_ActiveEdge ***
	//*** Values for DAQmx_SampClk_Timebase_ActiveEdge ***
	//*** Values for DAQmx_AIConv_ActiveEdge ***
	//*** Values for DAQmx_DigEdge_StartTrig_Edge ***
	//*** Values for DAQmx_DigEdge_RefTrig_Edge ***
	//*** Values for DAQmx_DigEdge_AdvTrig_Edge ***
	//*** Values for DAQmx_DigEdge_ArmStartTrig_Edge ***
	//*** Values for DAQmx_DigEdge_WatchdogExpirTrig_Edge ***
	//*** Value set Edge1 ***
//	static public final int DAQmx_Val_Rising = 10280; // Rising
//	static public final int DAQmx_Val_Falling = 10171; // Falling

	//*** Values for DAQmx_CI_Encoder_DecodingType ***
	//*** Value set EncoderType2 ***
	static public final int DAQmx_Val_X1 = 10090; // X1
	static public final int DAQmx_Val_X2 = 10091; // X2
	static public final int DAQmx_Val_X4 = 10092; // X4
	static public final int DAQmx_Val_TwoPulseCounting = 10313; // Two Pulse Counting

	//*** Values for DAQmx_CI_Encoder_ZIndexPhase ***
	//*** Value set EncoderZIndexPhase1 ***
	static public final int DAQmx_Val_AHighBHigh = 10040; // A High B High
	static public final int DAQmx_Val_AHighBLow = 10041; // A High B Low
	static public final int DAQmx_Val_ALowBHigh = 10042; // A Low B High
	static public final int DAQmx_Val_ALowBLow = 10043; // A Low B Low

	//*** Values for DAQmx_AI_Excit_DCorAC ***
	//*** Value set ExcitationDCorAC ***
//	static public final int DAQmx_Val_DC = 10050; // DC
//	static public final int DAQmx_Val_AC = 10045; // AC

	//*** Values for DAQmx_AI_Excit_Src ***
	//*** Value set ExcitationSource ***
//	static public final int DAQmx_Val_Internal = 10200; // Internal
//	static public final int DAQmx_Val_External = 10167; // External
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_AI_Excit_VoltageOrCurrent ***
	//*** Value set ExcitationVoltageOrCurrent ***
//	static public final int DAQmx_Val_Voltage = 10322; // Voltage
//	static public final int DAQmx_Val_Current = 10134; // Current

	//*** Values for DAQmx_Exported_CtrOutEvent_OutputBehavior ***
	//*** Value set ExportActions2 ***
	static public final int DAQmx_Val_Pulse = 10265; // Pulse
	static public final int DAQmx_Val_Toggle = 10307; // Toggle

	//*** Values for DAQmx_Exported_SampClk_OutputBehavior ***
	//*** Value set ExportActions3 ***
//	static public final int DAQmx_Val_Pulse = 10265; // Pulse
	static public final int DAQmx_Val_Lvl = 10210; // Level

	//*** Values for DAQmx_Exported_HshkEvent_OutputBehavior ***
	//*** Value set ExportActions5 ***
	static public final int DAQmx_Val_Interlocked = 12549; // Interlocked
//	static public final int DAQmx_Val_Pulse = 10265; // Pulse

	//*** Values for DAQmx_AI_Freq_Units ***
	//*** Value set FrequencyUnits ***
	static public final int DAQmx_Val_Hz = 10373; // Hz
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_CO_Pulse_Freq_Units ***
	//*** Value set FrequencyUnits2 ***
//	static public final int DAQmx_Val_Hz = 10373; // Hz

	//*** Values for DAQmx_CI_Freq_Units ***
	//*** Value set FrequencyUnits3 ***
//	static public final int DAQmx_Val_Hz = 10373; // Hz
//	static public final int DAQmx_Val_Ticks = 10304; // Ticks
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_AO_FuncGen_Type ***
	//*** Value set FuncGenType ***
	static public final int DAQmx_Val_Sine = 14751; // Sine
	static public final int DAQmx_Val_Triangle = 14752; // Triangle
	static public final int DAQmx_Val_Square = 14753; // Square
	static public final int DAQmx_Val_Sawtooth = 14754; // Sawtooth

	//*** Values for DAQmx_CI_GPS_SyncMethod ***
	//*** Value set GpsSignalType1 ***
	static public final int DAQmx_Val_IRIGB = 10070; // IRIG-B
	static public final int DAQmx_Val_PPS = 10080; // PPS
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_Hshk_StartCond ***
	//*** Value set HandshakeStartCondition ***
	static public final int DAQmx_Val_Immediate = 10198; // Immediate
	static public final int DAQmx_Val_WaitForHandshakeTriggerAssert = 12550; // Wait For Handshake Trigger Assert
	static public final int DAQmx_Val_WaitForHandshakeTriggerDeassert = 12551; // Wait For Handshake Trigger Deassert


	//*** Values for DAQmx_AI_DataXferReqCond ***
	//*** Values for DAQmx_DI_DataXferReqCond ***
	//*** Value set InputDataTransferCondition ***
	static public final int DAQmx_Val_OnBrdMemMoreThanHalfFull = 10237; // Onboard Memory More than Half Full
	static public final int DAQmx_Val_OnBrdMemNotEmpty = 10241; // Onboard Memory Not Empty
//	static public final int DAQmx_Val_OnbrdMemCustomThreshold = 12577; // Onboard Memory Custom Threshold
	static public final int DAQmx_Val_WhenAcqComplete = 12546; // When Acquisition Complete

	//*** Values for DAQmx_AI_TermCfg ***
	//*** Value set InputTermCfg ***
	static public final int DAQmx_Val_RSE = 10083; // RSE
	static public final int DAQmx_Val_NRSE = 10078; // NRSE
	static public final int DAQmx_Val_Diff = 10106; // Differential
	static public final int DAQmx_Val_PseudoDiff = 12529; // Pseudodifferential

	//*** Values for DAQmx_AI_LVDT_SensitivityUnits ***
	//*** Value set LVDTSensitivityUnits1 ***
	static public final int DAQmx_Val_mVoltsPerVoltPerMillimeter = 12506; // mVolts/Volt/mMeter
	static public final int DAQmx_Val_mVoltsPerVoltPerMilliInch = 12505; // mVolts/Volt/0.001 Inch

	//*** Values for DAQmx_AI_LVDT_Units ***
	//*** Value set LengthUnits2 ***
	static public final int DAQmx_Val_Meters = 10219; // Meters
	static public final int DAQmx_Val_Inches = 10379; // Inches
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_CI_LinEncoder_Units ***
	//*** Value set LengthUnits3 ***
//	static public final int DAQmx_Val_Meters = 10219; // Meters
//	static public final int DAQmx_Val_Inches = 10379; // Inches
//	static public final int DAQmx_Val_Ticks = 10304; // Ticks
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_CI_OutputState ***
	//*** Values for DAQmx_CO_Pulse_IdleState ***
	//*** Values for DAQmx_CO_OutputState ***
	//*** Values for DAQmx_Exported_CtrOutEvent_Toggle_IdleState ***
	//*** Values for DAQmx_Exported_HshkEvent_Interlocked_AssertedLvl ***
	//*** Values for DAQmx_Interlocked_HshkTrig_AssertedLvl ***
	//*** Values for DAQmx_DigLvl_PauseTrig_When ***
	//*** Value set Level1 ***
//	static public final int DAQmx_Val_High = 10192; // High
//	static public final int DAQmx_Val_Low = 10214; // Low

	//*** Values for DAQmx_DI_LogicFamily ***
	//*** Values for DAQmx_DO_LogicFamily ***
	//*** Value set LogicFamily ***
	static public final int DAQmx_Val_2point5V = 14620; // 2.5 V
	static public final int DAQmx_Val_3point3V = 14621; // 3.3 V
	static public final int DAQmx_Val_5V = 14619; // 5.0 V

	//*** Values for DAQmx_AIConv_Timebase_Src ***
	//*** Value set MIOAIConvertTbSrc ***
	static public final int DAQmx_Val_SameAsSampTimebase = 10284; // Same as Sample Timebase
	static public final int DAQmx_Val_SameAsMasterTimebase = 10282; // Same as Master Timebase
	static public final int DAQmx_Val_20MHzTimebase = 12537; // 20MHz Timebase
	static public final int DAQmx_Val_80MHzTimebase = 14636; // 80MHz Timebase

	//*** Values for DAQmx_AO_FuncGen_ModulationType ***
	//*** Value set ModulationType ***
	static public final int DAQmx_Val_AM = 14756; // AM
	static public final int DAQmx_Val_FM = 14757; // FM
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_AO_DataXferReqCond ***
	//*** Values for DAQmx_DO_DataXferReqCond ***
	//*** Value set OutputDataTransferCondition ***
	static public final int DAQmx_Val_OnBrdMemEmpty = 10235; // Onboard Memory Empty
	static public final int DAQmx_Val_OnBrdMemHalfFullOrLess = 10239; // Onboard Memory Half Full or Less
	static public final int DAQmx_Val_OnBrdMemNotFull = 10242; // Onboard Memory Less than Full

	//*** Values for DAQmx_AO_TermCfg ***
	//*** Value set OutputTermCfg ***
//	static public final int DAQmx_Val_RSE = 10083; // RSE
//	static public final int DAQmx_Val_Diff = 10106; // Differential
//	static public final int DAQmx_Val_PseudoDiff = 12529; // Pseudodifferential

	//*** Values for DAQmx_Read_OverWrite ***
	//*** Value set OverwriteMode1 ***
	static public final int DAQmx_Val_OverwriteUnreadSamps = 10252; // Overwrite Unread Samples
	static public final int DAQmx_Val_DoNotOverwriteUnreadSamps = 10159; // Do Not Overwrite Unread Samples

	//*** Values for DAQmx_Exported_AIConvClk_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_SampClk_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_AdvTrig_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_PauseTrig_Lvl_ActiveLvl ***
	//*** Values for DAQmx_Exported_RefTrig_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_StartTrig_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_AdvCmpltEvent_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_AIHoldCmpltEvent_PulsePolarity ***
	//*** Values for DAQmx_Exported_ChangeDetectEvent_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_CtrOutEvent_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_HshkEvent_Pulse_Polarity ***
	//*** Values for DAQmx_Exported_RdyForXferEvent_Lvl_ActiveLvl ***
	//*** Values for DAQmx_Exported_DataActiveEvent_Lvl_ActiveLvl ***
	//*** Values for DAQmx_Exported_RdyForStartEvent_Lvl_ActiveLvl ***
	//*** Value set Polarity2 ***
	static public final int DAQmx_Val_ActiveHigh = 10095; // Active High
	static public final int DAQmx_Val_ActiveLow = 10096; // Active Low

	//*** Values for DAQmx_Dev_ProductCategory ***
	//*** Value set ProductCategory ***
	static public final int DAQmx_Val_MSeriesDAQ = 14643; // M Series DAQ
	static public final int DAQmx_Val_ESeriesDAQ = 14642; // E Series DAQ
	static public final int DAQmx_Val_SSeriesDAQ = 14644; // S Series DAQ
	static public final int DAQmx_Val_BSeriesDAQ = 14662; // B Series DAQ
	static public final int DAQmx_Val_SCSeriesDAQ = 14645; // SC Series DAQ
	static public final int DAQmx_Val_USBDAQ = 14646; // USB DAQ
	static public final int DAQmx_Val_AOSeries = 14647; // AO Series
	static public final int DAQmx_Val_DigitalIO = 14648; // Digital I/O
	static public final int DAQmx_Val_TIOSeries = 14661; // TIO Series
	static public final int DAQmx_Val_DynamicSignalAcquisition = 14649; // Dynamic Signal Acquisition
	static public final int DAQmx_Val_Switches = 14650; // Switches
	static public final int DAQmx_Val_CompactDAQChassis = 14658; // CompactDAQ Chassis
	static public final int DAQmx_Val_CSeriesModule = 14659; // C Series Module
	static public final int DAQmx_Val_SCXIModule = 14660; // SCXI Module
	static public final int DAQmx_Val_SCCConnectorBlock = 14704; // SCC Connector Block
	static public final int DAQmx_Val_SCCModule = 14705; // SCC Module
	static public final int DAQmx_Val_NIELVIS = 14755; // NI ELVIS
	static public final int DAQmx_Val_NetworkDAQ = 14829; // Network DAQ
//	static public final int DAQmx_Val_Unknown = 12588; // Unknown

	//*** Values for DAQmx_AI_RTD_Type ***
	//*** Value set RTDType1 ***
	static public final int DAQmx_Val_Pt3750 = 12481; // Pt3750
	static public final int DAQmx_Val_Pt3851 = 10071; // Pt3851
	static public final int DAQmx_Val_Pt3911 = 12482; // Pt3911
	static public final int DAQmx_Val_Pt3916 = 10069; // Pt3916
	static public final int DAQmx_Val_Pt3920 = 10053; // Pt3920
	static public final int DAQmx_Val_Pt3928 = 12483; // Pt3928
	static public final int DAQmx_Val_Custom = 10137; // Custom

	//*** Values for DAQmx_AI_RVDT_SensitivityUnits ***
	//*** Value set RVDTSensitivityUnits1 ***
	static public final int DAQmx_Val_mVoltsPerVoltPerDegree = 12507; // mVolts/Volt/Degree
	static public final int DAQmx_Val_mVoltsPerVoltPerRadian = 12508; // mVolts/Volt/Radian

	//*** Values for DAQmx_AI_RawDataCompressionType ***
	//*** Value set RawDataCompressionType ***
//	static public final int DAQmx_Val_None = 10230; // None
	static public final int DAQmx_Val_LosslessPacking = 12555; // Lossless Packing
	static public final int DAQmx_Val_LossyLSBRemoval = 12556; // Lossy LSB Removal

	//*** Values for DAQmx_Read_RelativeTo ***
	//*** Value set ReadRelativeTo ***
	static public final int DAQmx_Val_FirstSample = 10424; // First Sample
	static public final int DAQmx_Val_CurrReadPos = 10425; // Current Read Position
	static public final int DAQmx_Val_RefTrig = 10426; // Reference Trigger
	static public final int DAQmx_Val_FirstPretrigSamp = 10427; // First Pretrigger Sample
	static public final int DAQmx_Val_MostRecentSamp = 10428; // Most Recent Sample

	//*** Values for DAQmx_Write_RegenMode ***
	//*** Value set RegenerationMode1 ***
	static public final int DAQmx_Val_AllowRegen = 10097; // Allow Regeneration
	static public final int DAQmx_Val_DoNotAllowRegen = 10158; // Do Not Allow Regeneration

	//*** Values for DAQmx_AI_ResistanceCfg ***
	//*** Value set ResistanceConfiguration ***
	static public final int DAQmx_Val_2Wire = 2; // 2-Wire
	static public final int DAQmx_Val_3Wire = 3; // 3-Wire
//	static public final int DAQmx_Val_4Wire = 4; // 4-Wire

	//*** Values for DAQmx_AI_Resistance_Units ***
	//*** Value set ResistanceUnits1 ***
	static public final int DAQmx_Val_Ohms = 10384; // Ohms
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale
//	static public final int DAQmx_Val_FromTEDS = 12516; // From TEDS

	//*** Value set ResistanceUnits2 ***
//	static public final int DAQmx_Val_Ohms = 10384; // Ohms
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_AI_ResolutionUnits ***
	//*** Values for DAQmx_AO_ResolutionUnits ***
	//*** Value set ResolutionType1 ***
	static public final int DAQmx_Val_Bits = 10109; // Bits

	//*** Value set SCXI1124Range ***
	static public final int DAQmx_Val_SCXI1124Range0to1V = 14629; // 0V to 1V
	static public final int DAQmx_Val_SCXI1124Range0to5V = 14630; // 0V to 5V
	static public final int DAQmx_Val_SCXI1124Range0to10V = 14631; // 0V to 10V
	static public final int DAQmx_Val_SCXI1124RangeNeg1to1V = 14632; // -1V to 1V
	static public final int DAQmx_Val_SCXI1124RangeNeg5to5V = 14633; // -5V to 5V
	static public final int DAQmx_Val_SCXI1124RangeNeg10to10V = 14634; // -10V to 10V
	static public final int DAQmx_Val_SCXI1124Range0to20mA = 14635; // 0mA to 20mA

	//*** Values for DAQmx_DI_AcquireOn ***
	//*** Values for DAQmx_DO_GenerateOn ***
	//*** Value set SampleClockActiveOrInactiveEdgeSelection ***
	static public final int DAQmx_Val_SampClkActiveEdge = 14617; // Sample Clock Active Edge
	static public final int DAQmx_Val_SampClkInactiveEdge = 14618; // Sample Clock Inactive Edge

	//*** Values for DAQmx_Hshk_SampleInputDataWhen ***
	//*** Value set SampleInputDataWhen ***
	static public final int DAQmx_Val_HandshakeTriggerAsserts = 12552; // Handshake Trigger Asserts
	static public final int DAQmx_Val_HandshakeTriggerDeasserts = 12553; // Handshake Trigger Deasserts

	//*** Values for DAQmx_SampTimingType ***
	//*** Value set SampleTimingType ***
	static public final int DAQmx_Val_SampClk = 10388; // Sample Clock
	static public final int DAQmx_Val_BurstHandshake = 12548; // Burst Handshake
	static public final int DAQmx_Val_Handshake = 10389; // Handshake
	static public final int DAQmx_Val_Implicit = 10451; // Implicit
	static public final int DAQmx_Val_OnDemand = 10390; // On Demand
	static public final int DAQmx_Val_ChangeDetection = 12504; // Change Detection
	static public final int DAQmx_Val_PipelinedSampClk = 14668; // Pipelined Sample Clock

	//*** Values for DAQmx_Scale_Type ***
	//*** Value set ScaleType ***
	static public final int DAQmx_Val_Linear = 10447; // Linear
	static public final int DAQmx_Val_MapRanges = 10448; // Map Ranges
	static public final int DAQmx_Val_Polynomial = 10449; // Polynomial
	static public final int DAQmx_Val_Table = 10450; // Table

	//*** Values for DAQmx_AI_Thrmcpl_ScaleType ***
	//*** Value set ScaleType2 ***
//	static public final int DAQmx_Val_Polynomial = 10449; // Polynomial
//	static public final int DAQmx_Val_Table = 10450; // Table

	//*** Values for DAQmx_AI_ChanCal_ScaleType ***
	//*** Value set ScaleType3 ***
//	static public final int DAQmx_Val_Polynomial = 10449; // Polynomial
//	static public final int DAQmx_Val_Table = 10450; // Table
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_AI_Bridge_ShuntCal_Select ***
	//*** Value set ShuntCalSelect ***
	static public final int DAQmx_Val_A = 12513; // A
	static public final int DAQmx_Val_B = 12514; // B
	static public final int DAQmx_Val_AandB = 12515; // A and B

	//*** Value set ShuntElementLocation ***
	static public final int DAQmx_Val_R1 = 12465; // R1
	static public final int DAQmx_Val_R2 = 12466; // R2
	static public final int DAQmx_Val_R3 = 12467; // R3
	static public final int DAQmx_Val_R4 = 14813; // R4
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Value set Signal ***
	static public final int DAQmx_Val_AIConvertClock = 12484; // AI Convert Clock
	static public final int DAQmx_Val_10MHzRefClock = 12536; // 10MHz Reference Clock
	static public final int DAQmx_Val_20MHzTimebaseClock = 12486; // 20MHz Timebase Clock
	static public final int DAQmx_Val_SampleClock = 12487; // Sample Clock
	static public final int DAQmx_Val_ReferenceTrigger = 12490; // Reference Trigger
	static public final int DAQmx_Val_StartTrigger = 12491; // Start Trigger
	static public final int DAQmx_Val_AdvCmpltEvent = 12492; // Advance Complete Event
	static public final int DAQmx_Val_AIHoldCmpltEvent = 12493; // AI Hold Complete Event
	static public final int DAQmx_Val_CounterOutputEvent = 12494; // Counter Output Event
	static public final int DAQmx_Val_ChangeDetectionEvent = 12511; // Change Detection Event
	static public final int DAQmx_Val_WDTExpiredEvent = 12512; // Watchdog Timer Expired Event

	//*** Value set Signal2 ***
	static public final int DAQmx_Val_SampleCompleteEvent = 12530; // Sample Complete Event
//	static public final int DAQmx_Val_CounterOutputEvent = 12494; // Counter Output Event
//	static public final int DAQmx_Val_ChangeDetectionEvent = 12511; // Change Detection Event
//	static public final int DAQmx_Val_SampleClock = 12487; // Sample Clock

	//*** Values for DAQmx_AnlgEdge_StartTrig_Slope ***
	//*** Values for DAQmx_AnlgEdge_RefTrig_Slope ***
	//*** Value set Slope1 ***
	static public final int DAQmx_Val_RisingSlope = 10280; // Rising
	static public final int DAQmx_Val_FallingSlope = 10171; // Falling

	//*** Values for DAQmx_AI_SoundPressure_Units ***
	//*** Value set SoundPressureUnits1 ***
//	static public final int DAQmx_Val_Pascals = 10081; // Pascals
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_AI_Lowpass_SwitchCap_ClkSrc ***
	//*** Values for DAQmx_AO_DAC_Ref_Src ***
	//*** Values for DAQmx_AO_DAC_Offset_Src ***
	//*** Value set SourceSelection ***
//	static public final int DAQmx_Val_Internal = 10200; // Internal
//	static public final int DAQmx_Val_External = 10167; // External

	//*** Values for DAQmx_AI_StrainGage_Cfg ***
	//*** Value set StrainGageBridgeType1 ***
	static public final int DAQmx_Val_FullBridgeI = 10183; // Full Bridge I
	static public final int DAQmx_Val_FullBridgeII = 10184; // Full Bridge II
	static public final int DAQmx_Val_FullBridgeIII = 10185; // Full Bridge III
	static public final int DAQmx_Val_HalfBridgeI = 10188; // Half Bridge I
	static public final int DAQmx_Val_HalfBridgeII = 10189; // Half Bridge II
	static public final int DAQmx_Val_QuarterBridgeI = 10271; // Quarter Bridge I
	static public final int DAQmx_Val_QuarterBridgeII = 10272; // Quarter Bridge II

	//*** Values for DAQmx_AI_Strain_Units ***
	//*** Value set StrainUnits1 ***
//	static public final int DAQmx_Val_Strain = 10299; // Strain
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_SwitchScan_RepeatMode ***
	//*** Value set SwitchScanRepeatMode ***
	static public final int DAQmx_Val_Finite = 10172; // Finite
	static public final int DAQmx_Val_Cont = 10117; // Continuous

	//*** Values for DAQmx_SwitchChan_Usage ***
	//*** Value set SwitchUsageTypes ***
	static public final int DAQmx_Val_Source = 10439; // Source
	static public final int DAQmx_Val_Load = 10440; // Load
	static public final int DAQmx_Val_ReservedForRouting = 10441; // Reserved for Routing

	//*** Value set TEDSUnits ***
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale
//	static public final int DAQmx_Val_FromTEDS = 12516; // From TEDS

	//*** Values for DAQmx_AI_Temp_Units ***
	//*** Value set TemperatureUnits1 ***
//	static public final int DAQmx_Val_DegC = 10143; // Deg C
//	static public final int DAQmx_Val_DegF = 10144; // Deg F
//	static public final int DAQmx_Val_Kelvins = 10325; // Kelvins
//	static public final int DAQmx_Val_DegR = 10145; // Deg R
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_AI_Thrmcpl_Type ***
	//*** Value set ThermocoupleType1 ***
	static public final int DAQmx_Val_J_Type_TC = 10072; // J
	static public final int DAQmx_Val_K_Type_TC = 10073; // K
	static public final int DAQmx_Val_N_Type_TC = 10077; // N
	static public final int DAQmx_Val_R_Type_TC = 10082; // R
	static public final int DAQmx_Val_S_Type_TC = 10085; // S
	static public final int DAQmx_Val_T_Type_TC = 10086; // T
	static public final int DAQmx_Val_B_Type_TC = 10047; // B
	static public final int DAQmx_Val_E_Type_TC = 10055; // E

	//*** Values for DAQmx_CI_Timestamp_Units ***
	//*** Value set TimeUnits ***
//	static public final int DAQmx_Val_Seconds = 10364; // Seconds
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_CO_Pulse_Time_Units ***
	//*** Value set TimeUnits2 ***
//	static public final int DAQmx_Val_Seconds = 10364; // Seconds

	//*** Values for DAQmx_CI_Period_Units ***
	//*** Values for DAQmx_CI_PulseWidth_Units ***
	//*** Values for DAQmx_CI_TwoEdgeSep_Units ***
	//*** Values for DAQmx_CI_SemiPeriod_Units ***
	//*** Value set TimeUnits3 ***
//	static public final int DAQmx_Val_Seconds = 10364; // Seconds
//	static public final int DAQmx_Val_Ticks = 10304; // Ticks
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Value set TimingResponseMode ***
	static public final int DAQmx_Val_SingleCycle = 14613; // Single-cycle
	static public final int DAQmx_Val_Multicycle = 14614; // Multicycle

	//*** Values for DAQmx_ArmStartTrig_Type ***
	//*** Values for DAQmx_WatchdogExpirTrig_Type ***
	//*** Value set TriggerType4 ***
//	static public final int DAQmx_Val_DigEdge = 10150; // Digital Edge
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_AdvTrig_Type ***
	//*** Value set TriggerType5 ***
//	static public final int DAQmx_Val_DigEdge = 10150; // Digital Edge
	static public final int DAQmx_Val_Software = 10292; // Software
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_PauseTrig_Type ***
	//*** Value set TriggerType6 ***
	static public final int DAQmx_Val_AnlgLvl = 10101; // Analog Level
	static public final int DAQmx_Val_AnlgWin = 10103; // Analog Window
	static public final int DAQmx_Val_DigLvl = 10152; // Digital Level
	static public final int DAQmx_Val_DigPattern = 10398; // Digital Pattern
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_StartTrig_Type ***
	//*** Values for DAQmx_RefTrig_Type ***
	//*** Value set TriggerType8 ***
	static public final int DAQmx_Val_AnlgEdge = 10099; // Analog Edge
	static public final int DAQmx_Val_DigEdge = 10150; // Digital Edge
//	static public final int DAQmx_Val_DigPattern = 10398; // Digital Pattern
//	static public final int DAQmx_Val_AnlgWin = 10103; // Analog Window
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_HshkTrig_Type ***
	//*** Value set TriggerType9 ***
//	static public final int DAQmx_Val_Interlocked = 12549; // Interlocked
//	static public final int DAQmx_Val_None = 10230; // None

	//*** Values for DAQmx_SampClk_UnderflowBehavior ***
	//*** Value set UnderflowBehavior ***
	static public final int DAQmx_Val_HaltOutputAndError = 14615; // Halt Output and Error
	static public final int DAQmx_Val_PauseUntilDataAvailable = 14616; // Pause until Data Available

	//*** Values for DAQmx_Scale_PreScaledUnits ***
	//*** Value set UnitsPreScaled ***
//	static public final int DAQmx_Val_Volts = 10348; // Volts
//	static public final int DAQmx_Val_Amps = 10342; // Amps
//	static public final int DAQmx_Val_DegF = 10144; // Deg F
//	static public final int DAQmx_Val_DegC = 10143; // Deg C
//	static public final int DAQmx_Val_DegR = 10145; // Deg R
//	static public final int DAQmx_Val_Kelvins = 10325; // Kelvins
	static public final int DAQmx_Val_Strain = 10299; // Strain
//	static public final int DAQmx_Val_Ohms = 10384; // Ohms
//	static public final int DAQmx_Val_Hz = 10373; // Hz
//	static public final int DAQmx_Val_Seconds = 10364; // Seconds
//	static public final int DAQmx_Val_Meters = 10219; // Meters
//	static public final int DAQmx_Val_Inches = 10379; // Inches
//	static public final int DAQmx_Val_Degrees = 10146; // Degrees
//	static public final int DAQmx_Val_Radians = 10273; // Radians
	static public final int DAQmx_Val_g = 10186; // g
//	static public final int DAQmx_Val_MetersPerSecondSquared = 12470; // m/s^2
	static public final int DAQmx_Val_Pascals = 10081; // Pascals
//	static public final int DAQmx_Val_FromTEDS = 12516; // From TEDS

	//*** Values for DAQmx_AI_Voltage_Units ***
	//*** Values for DAQmx_AI_Voltage_ACRMS_Units ***
	//*** Value set VoltageUnits1 ***
//	static public final int DAQmx_Val_Volts = 10348; // Volts
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale
//	static public final int DAQmx_Val_FromTEDS = 12516; // From TEDS

	//*** Values for DAQmx_AO_Voltage_Units ***
	//*** Value set VoltageUnits2 ***
//	static public final int DAQmx_Val_Volts = 10348; // Volts
//	static public final int DAQmx_Val_FromCustomScale = 10065; // From Custom Scale

	//*** Values for DAQmx_Read_WaitMode ***
	//*** Value set WaitMode ***
//	static public final int DAQmx_Val_WaitForInterrupt = 12523; // Wait For Interrupt
//	static public final int DAQmx_Val_Poll = 12524; // Poll
//	static public final int DAQmx_Val_Yield = 12525; // Yield
//	static public final int DAQmx_Val_Sleep = 12547; // Sleep

	//*** Values for DAQmx_Write_WaitMode ***
	//*** Value set WaitMode2 ***
//	static public final int DAQmx_Val_Poll = 12524; // Poll
	static public final int DAQmx_Val_Yield = 12525; // Yield
	static public final int DAQmx_Val_Sleep = 12547; // Sleep

	//*** Values for DAQmx_RealTime_WaitForNextSampClkWaitMode ***
	//*** Value set WaitMode3 ***
//	static public final int DAQmx_Val_WaitForInterrupt = 12523; // Wait For Interrupt
//	static public final int DAQmx_Val_Poll = 12524; // Poll

	//*** Values for DAQmx_RealTime_WriteRecoveryMode ***
	//*** Value set WaitMode4 ***
	static public final int DAQmx_Val_WaitForInterrupt = 12523; // Wait For Interrupt
	static public final int DAQmx_Val_Poll = 12524; // Poll

	//*** Values for DAQmx_AnlgWin_StartTrig_When ***
	//*** Values for DAQmx_AnlgWin_RefTrig_When ***
	//*** Value set WindowTriggerCondition1 ***
	static public final int DAQmx_Val_EnteringWin = 10163; // Entering Window
	static public final int DAQmx_Val_LeavingWin = 10208; // Leaving Window

	//*** Values for DAQmx_AnlgWin_PauseTrig_When ***
	//*** Value set WindowTriggerCondition2 ***
	static public final int DAQmx_Val_InsideWin = 10199; // Inside Window
	static public final int DAQmx_Val_OutsideWin = 10251; // Outside Window

	//*** Value set WriteBasicTEDSOptions ***
	static public final int DAQmx_Val_WriteToEEPROM = 12538; // Write To EEPROM
	static public final int DAQmx_Val_WriteToPROM = 12539; // Write To PROM Once
	static public final int DAQmx_Val_DoNotWrite = 12540; // Do Not Write

	//*** Values for DAQmx_Write_RelativeTo ***
	//*** Value set WriteRelativeTo ***
//	static public final int DAQmx_Val_FirstSample = 10424; // First Sample
	static public final int DAQmx_Val_CurrWritePos = 10430; // Current Write Position

}
