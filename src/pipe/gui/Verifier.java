package pipe.gui;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import dk.aau.cs.verification.VerifyTAPN.*;
import pipe.dataLayer.TAPNQuery;
import pipe.gui.widgets.RunningVerificationDialog;
import dk.aau.cs.TCTL.TCTLAbstractProperty;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.translations.ReductionOption;
import dk.aau.cs.util.VerificationCallback;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.UPPAAL.UppaalIconSelector;
import dk.aau.cs.verification.UPPAAL.Verifyta;
import dk.aau.cs.verification.UPPAAL.VerifytaOptions;

import java.io.File;
import java.io.IOException;

/**
 * Implementes af class for handling integrated Uppaal Verification
 * 
 * Copyright 2009 Author Kenneth Yrke Joergensen <kenneth@yrke.dk>
 * 
 * Licensed under the Open Software License version 3.0
 */

public class Verifier {
    private static File reducedNetTempFile = null;

	private static Verifyta getVerifyta() {
		Verifyta verifyta = new Verifyta(new FileFinder(), new MessengerImpl());
		verifyta.setup();
		return verifyta;
	}

	private static VerifyTAPN getVerifyTAPN() {
		VerifyTAPN verifytapn = new VerifyTAPN(new FileFinder(), new MessengerImpl());
		verifytapn.setup();
		return verifytapn;
	}

	private static VerifyTAPNDiscreteVerification getVerifydTAPN() {
		VerifyTAPNDiscreteVerification verifydtapn = new VerifyTAPNDiscreteVerification(new FileFinder(), new MessengerImpl());
		verifydtapn.setup();
		return verifydtapn;
	}

	private static VerifyPN getVerifyPN() {
		VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());
		verifypn.setup();
		return verifypn;
	}

	public static ModelChecker getModelChecker(TAPNQuery query) {
		if(query.getReductionOption() == ReductionOption.VerifyTAPN){
			return getVerifyTAPN();
		} else if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification){
			return getVerifydTAPN();
		} else if(query.getReductionOption() == ReductionOption.VerifyPN){
			return getVerifyPN();
		}
		else{
			throw new RuntimeException("Verification method: " + query.getReductionOption() + ", should not be send here");
		}
	}

	public static String getReducedNetFilePath() {
	    return reducedNetTempFile.getAbsolutePath();
    }

	public static void analyzeKBound(TimedArcPetriNetNetwork tapnNetwork, int k, JSpinner tokensControl) {
		ModelChecker modelChecker;
		
		if(tapnNetwork.isUntimed()){
			modelChecker = getVerifyPN();
		} else if(tapnNetwork.hasWeights() || tapnNetwork.hasUrgentTransitions() || tapnNetwork.hasUncontrollableTransitions()){
			modelChecker = getVerifydTAPN();
		} else {
			modelChecker = getVerifyTAPN();
		}

		if (!modelChecker.isCorrectVersion()) {
			System.err.println("The model checker not found, or you are running an old version of it.\n"
							+ "Update to the latest development version.");
			return;
		}
		KBoundAnalyzer optimizer = new KBoundAnalyzer(tapnNetwork, k, modelChecker, new MessengerImpl(), tokensControl);
		optimizer.analyze();
	}


	public static void runUppaalVerification(TimedArcPetriNetNetwork timedArcPetriNetNetwork, TAPNQuery input) {
		Verifyta verifyta = getVerifyta();
		if (!verifyta.isCorrectVersion()) {
			System.err.println("Verifyta not found, or you are running an old version of Verifyta.\n"
							+ "Update to the latest development version.");
			return;
		}

		TCTLAbstractProperty inputQuery = input.getProperty();

		VerifytaOptions verifytaOptions = new VerifytaOptions(
				input.getTraceOption(),
				input.getSearchOption(),
				false,
				input.getReductionOption(),
				input.useSymmetry(),
				input.useOverApproximation(),
				input.isOverApproximationEnabled(),
				input.isUnderApproximationEnabled(),
				input.approximationDenominator()
		);

		if (inputQuery == null) {
			return;
		}

		if (timedArcPetriNetNetwork != null) {
			RunVerificationBase thread = new RunVerification(verifyta, new UppaalIconSelector(), new MessengerImpl());
			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp(), thread);
			thread.execute(
					verifytaOptions,
					timedArcPetriNetNetwork,
					new dk.aau.cs.model.tapn.TAPNQuery(input.getProperty(), input.getCapacity()),
					null
			);
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

	}

	public static void runVerifyTAPNVerification(
			TimedArcPetriNetNetwork tapnNetwork,
			TAPNQuery query,
            boolean onlyCreateReducedNet,
			VerificationCallback callback
	) {
		ModelChecker verifytapn = getModelChecker(query);


		if (!verifytapn.isCorrectVersion()) {
			new MessengerImpl().displayErrorMessage(
					"No "+verifytapn+" specified: The verification is cancelled",
					"Verification Error");
			return;
		}
		
		TCTLAbstractProperty inputQuery = query.getProperty();

		int bound = query.getCapacity();
		
		VerifyTAPNOptions verifytapnOptions;
		if(query.getReductionOption() == ReductionOption.VerifyTAPNdiscreteVerification){
			verifytapnOptions = new VerifyDTAPNOptions(
					bound,
					query.getTraceOption(),
					query.getSearchOption(),
					query.useSymmetry(),
					query.useGCD(),
					query.useTimeDarts(),
					query.usePTrie(),
					query.useOverApproximation(),
					query.discreteInclusion(),
					query.inclusionPlaces(),
					query.getWorkflowMode(),
					query.getStrongSoundnessBound(),
					query.isOverApproximationEnabled(),
					query.isUnderApproximationEnabled(),
					query.approximationDenominator(),
					query.isStubbornReductionEnabled()
			);
		} else if(query.getReductionOption() == ReductionOption.VerifyPN){
            try {
                reducedNetTempFile = File.createTempFile("reduced-", ".pnml");
            } catch (IOException e) {
                new MessengerImpl().displayErrorMessage(
                    e.getMessage(),
                    "Error");
                return;
            }

            verifytapnOptions = new VerifyPNOptions(
                bound,
                query.getTraceOption(),
                query.getSearchOption(),
                query.useOverApproximation(),
                query.useReduction()? ModelReduction.AGGRESSIVE:ModelReduction.NO_REDUCTION,
                query.isOverApproximationEnabled(),
                query.isUnderApproximationEnabled(),
                query.approximationDenominator(),
                query.getCategory(),
                query.getAlgorithmOption(),
                query.isSiphontrapEnabled(),
                query.isQueryReductionEnabled()? TAPNQuery.QueryReductionTime.UnlimitedTime: TAPNQuery.QueryReductionTime.NoTime,
                query.isStubbornReductionEnabled(),
                reducedNetTempFile.getAbsolutePath(),
                query.isTarOptionEnabled(),
                query.isTarjan(),
				query.useSMC(),
				query.useRuns(),
				query.useDepth()
            );
			System.out.println("big");
		} else {
			verifytapnOptions = new VerifyTAPNOptions(
					bound,
					query.getTraceOption(),
					query.getSearchOption(),
					query.useSymmetry(),
					query.useOverApproximation(),
					query.discreteInclusion(),
					query.inclusionPlaces(),
					query.isOverApproximationEnabled(),
					query.isUnderApproximationEnabled(),
					query.approximationDenominator()
			);
			System.out.println("small");
		}
		
		if (inputQuery == null) {
			return;
		}
		
		if (tapnNetwork != null) {
            RunVerificationBase thread;
		    if (reducedNetTempFile != null) {
                 thread = new RunVerification(verifytapn, new VerifyTAPNIconSelector(), new MessengerImpl(), callback, reducedNetTempFile.getAbsolutePath(), onlyCreateReducedNet );
            } else {
                thread = new RunVerification(verifytapn, new VerifyTAPNIconSelector(), new MessengerImpl(), callback);
            }

			RunningVerificationDialog dialog = new RunningVerificationDialog(CreateGui.getApp(), thread);
			thread.execute(verifytapnOptions, tapnNetwork, new dk.aau.cs.model.tapn.TAPNQuery(query.getProperty(), bound), query);
			dialog.setVisible(true);
		} else {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"There was an error converting the model.",
					"Conversion error", JOptionPane.ERROR_MESSAGE);
		}

		return;
		
	}
}
