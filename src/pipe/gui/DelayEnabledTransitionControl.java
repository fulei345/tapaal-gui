package pipe.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import dk.aau.cs.model.tapn.simulation.DelayMode;
import dk.aau.cs.model.tapn.simulation.ManualDelayMode;
import dk.aau.cs.model.tapn.simulation.RandomDelayMode;
import dk.aau.cs.model.tapn.simulation.ShortestDelayMode;

public class DelayEnabledTransitionControl extends JPanel{

	private static DelayMode defaultDelayMode = ShortestDelayMode.getInstance();
	private static BigDecimal defaultGranularity = new BigDecimal("0.1");

	private final JLabel precitionLabel;
	private final JSlider delayEnabledPrecision;
	private final JLabel delayModeLabel;
	private final JComboBox<DelayMode> delayMode;

	private DelayEnabledTransitionControl() {
		super(new GridBagLayout());
		
		//0 corresponds to 0.00001, 5 corresponds to 1 (   thus x corresponds to 1/(10^(5−x))  )
		delayEnabledPrecision = new JSlider(JSlider.HORIZONTAL, 0, 5, 4);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(0, new JLabel("0.00001"));
		labelTable.put(1, new JLabel("0.0001"));
		labelTable.put(2, new JLabel("0.001"));
		labelTable.put(3, new JLabel("0.01"));
		labelTable.put(4, new JLabel("0.1"));
		labelTable.put(5, new JLabel("1"));
		
		delayEnabledPrecision.setLabelTable(labelTable);
		delayEnabledPrecision.setSnapToTicks(true);
		delayEnabledPrecision.setMajorTickSpacing(1);
		delayEnabledPrecision.setPaintLabels(true);
		delayEnabledPrecision.setPaintTicks(true);
		delayEnabledPrecision.setPaintTrack(false);
		delayEnabledPrecision.setPreferredSize(new Dimension(340, delayEnabledPrecision.getPreferredSize().height));
		setValue(defaultGranularity);
		
		DelayMode[] items = {ShortestDelayMode.getInstance(), RandomDelayMode.getInstance(), ManualDelayMode.getInstance()};
		delayMode = new JComboBox<>(items);
		setDelayMode(defaultDelayMode);


		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(precitionLabel = new JLabel("Set the delay granularity"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(delayEnabledPrecision, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(delayModeLabel = new JLabel("Delay Mode:"), gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridx = 1;
		gbc.gridy = 2;
		add(delayMode, gbc);
		
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Delay controller"), 
				BorderFactory.createEmptyBorder(3, 3, 3, 3)));
	}
	
	private int getValueFromDecimal(BigDecimal decimal) {
		if(new BigDecimal("0.00001").compareTo(decimal) == 0) return 0;
		if(new BigDecimal("0.0001").compareTo(decimal) == 0) return 1;
		if(new BigDecimal("0.001").compareTo(decimal) == 0) return 2;
		if(new BigDecimal("0.01").compareTo(decimal) == 0) return 3;
		if(new BigDecimal("0.1").compareTo(decimal) == 0) return 4;
		if(new BigDecimal("1").compareTo(decimal) == 0) return 5;
		return 4;
	}

	//0 corresponds to 0.00001, 5 corresponds to 1 (   thus x corresponds to 1/(10^(5−x))  )
	public BigDecimal getValue(){ 
		return new BigDecimal(1.0/(Math.pow(10.0, (5.0- delayEnabledPrecision.getValue()))), new MathContext(Pipe.AGE_PRECISION));
	}
	
	public void setValue(BigDecimal value){
		delayEnabledPrecision.setValue(getValueFromDecimal(value));
	}
	
	public DelayMode getDelayMode(){
		return (DelayMode)delayMode.getSelectedItem();
	}
	
	public void setDelayMode(DelayMode delayMode){
		this.delayMode.setSelectedItem(delayMode);
	}



	private static DelayEnabledTransitionControl instance;
	
	public static DelayEnabledTransitionControl getInstance(){
		if(instance == null){
			instance = new DelayEnabledTransitionControl();
		}
		return instance;
	}
	
	public static void setDefaultDelayMode(DelayMode delayMode){
		defaultDelayMode = delayMode;
	}
	
	public static DelayMode getDefaultDelayMode(){
		if(instance != null){
			return getInstance().getDelayMode();
		} else {
			return defaultDelayMode;
		}
	}
	
	public static void setDefaultGranularity(BigDecimal granularity){
		defaultGranularity = granularity;
	}
	
	public static BigDecimal getDefaultGranularity(){
		if(instance != null){
			return getInstance().getValue();
		} else {
			return defaultGranularity;
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		precitionLabel.setEnabled(enabled);
		delayEnabledPrecision.setEnabled(enabled);
		delayModeLabel.setEnabled(enabled);
		delayMode.setEnabled(enabled);
	}
}
