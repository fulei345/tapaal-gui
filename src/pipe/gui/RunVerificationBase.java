package pipe.gui;

import java.io.File;
import java.util.concurrent.ExecutionException;

import javax.swing.*;

import dk.aau.cs.verification.VerifyTAPN.VerifyTAPNOptions;
import pipe.dataLayer.TAPNQuery.SearchOption;
import dk.aau.cs.Messenger;
import dk.aau.cs.TCTL.visitors.RenameAllPlacesVisitor;
import dk.aau.cs.TCTL.visitors.RenameAllTransitionsVisitor;
import dk.aau.cs.approximation.ApproximationWorker;
import dk.aau.cs.approximation.OverApproximation;
import dk.aau.cs.approximation.UnderApproximation;
import dk.aau.cs.model.tapn.TAPNQuery;
import dk.aau.cs.model.tapn.TimedArcPetriNet;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;
import dk.aau.cs.model.tapn.simulation.TAPNNetworkTrace;
import dk.aau.cs.model.tapn.simulation.TimedArcPetriNetTrace;
import dk.aau.cs.util.Tuple;
import dk.aau.cs.util.UnsupportedModelException;
import dk.aau.cs.verification.ITAPNComposer;
import dk.aau.cs.verification.ModelChecker;
import dk.aau.cs.verification.NameMapping;
import dk.aau.cs.verification.QueryType;
import dk.aau.cs.verification.TAPNComposer;
import dk.aau.cs.verification.TAPNTraceDecomposer;
import dk.aau.cs.verification.VerificationOptions;
import dk.aau.cs.verification.VerificationResult;
import dk.aau.cs.verification.VerifyTAPN.ModelReduction;
import dk.aau.cs.verification.VerifyTAPN.VerifyPN;
import dk.aau.cs.verification.VerifyTAPN.VerifyPNOptions;

public abstract class RunVerificationBase extends SwingWorker<VerificationResult<TAPNNetworkTrace>, Void> {

	protected ModelChecker modelChecker;

	protected VerificationOptions options;
	protected TimedArcPetriNetNetwork model;
	protected TAPNQuery query;
	protected pipe.dataLayer.TAPNQuery dataLayerQuery;
	protected String reducedNetFilePath;
	protected boolean reduceNetOnly;
	protected boolean reducedNetOpened = false;
	protected JSpinner spinner;

	protected Messenger messenger;

	public RunVerificationBase(ModelChecker modelChecker, Messenger messenger, String reducedNetFilePath, boolean reduceNetOnly, JSpinner spinner) {
		super();
		this.modelChecker = modelChecker;
		this.messenger = messenger;
		this.reducedNetFilePath = reducedNetFilePath;
		this.reduceNetOnly = reduceNetOnly;
        this.spinner = spinner;
    }

    public RunVerificationBase(ModelChecker modelChecker, Messenger messenger) {
        this(modelChecker, messenger, null, false, null);
    }

    public RunVerificationBase(ModelChecker modelChecker, Messenger messenger, JSpinner spinner) {
        this(modelChecker, messenger, null, false, spinner);
    }

	
	public void execute(VerificationOptions options, TimedArcPetriNetNetwork model, TAPNQuery query, pipe.dataLayer.TAPNQuery dataLayerQuery) {
		this.model = model;
		this.options = options;
		this.query = query;
		this.dataLayerQuery = dataLayerQuery;
		execute();
	}

	@Override
	protected VerificationResult<TAPNNetworkTrace> doInBackground() throws Exception {
		ITAPNComposer composer = new TAPNComposer(messenger, false);
		Tuple<TimedArcPetriNet, NameMapping> transformedModel = composer.transformModel(model);
		
		if (options.enabledOverApproximation())
		{
			OverApproximation overaprx = new OverApproximation();
			overaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
		}
		else if (options.enabledUnderApproximation())
		{
			UnderApproximation underaprx = new UnderApproximation();
			underaprx.modifyTAPN(transformedModel.value1(), options.approximationDenominator());
		}

		TAPNQuery clonedQuery = new TAPNQuery(query.getProperty().copy(), query.getExtraTokens());
		MapQueryToNewNames(clonedQuery, transformedModel.value2());
		
		if (dataLayerQuery != null){
			clonedQuery.setCategory(dataLayerQuery.getCategory()); // Used by the CTL engine
		}
		
		if(options.enabledStateequationsCheck()) {
			if ((query.queryType() == QueryType.EF || query.queryType() == QueryType.AG) && !query.hasDeadlock() &&
                !(options instanceof VerifyPNOptions)) {

                VerifyPN verifypn = new VerifyPN(new FileFinder(), new MessengerImpl());
				if (!verifypn.supportsModel(transformedModel.value1(), options)) {
					// Skip over-approximation if model is not supported.
					// Prevents verification from displaying error.
				}


				if (!verifypn.setup()) {
					messenger.displayInfoMessage("Over-approximation check is skipped because VerifyPN is not available.", "VerifyPN unavailable");
				} else {
					VerificationResult<TimedArcPetriNetTrace> overapprox_result = null;
					if (dataLayerQuery != null) {
						overapprox_result = verifypn.verify(
								new VerifyPNOptions(
										options.extraTokens(),
										options.traceOption(),
										SearchOption.OVERAPPROXIMATE,
										true,
										ModelReduction.AGGRESSIVE,
										options.enabledOverApproximation(),
										options.enabledUnderApproximation(),
										options.approximationDenominator(),
										dataLayerQuery.getCategory(),
										dataLayerQuery.getAlgorithmOption(),
										dataLayerQuery.isSiphontrapEnabled(),
										dataLayerQuery.isQueryReductionEnabled()? pipe.dataLayer.TAPNQuery.QueryReductionTime.UnlimitedTime: pipe.dataLayer.TAPNQuery.QueryReductionTime.NoTime,
										dataLayerQuery.isStubbornReductionEnabled(),
                                        reducedNetFilePath,
                                        dataLayerQuery.isTarOptionEnabled(),
                                        dataLayerQuery.isTarjan()
								),
								transformedModel,
								clonedQuery
						);
					} else {
						overapprox_result = verifypn.verify(
								new VerifyPNOptions(
										options.extraTokens(),
										options.traceOption(),
										SearchOption.OVERAPPROXIMATE,
										true,
										ModelReduction.AGGRESSIVE,
										options.enabledOverApproximation(),
										options.enabledUnderApproximation(),
										options.approximationDenominator(),
										pipe.dataLayer.TAPNQuery.QueryCategory.Default,
										pipe.dataLayer.TAPNQuery.AlgorithmOption.CERTAIN_ZERO,
										false,
                                        pipe.dataLayer.TAPNQuery.QueryReductionTime.UnlimitedTime,
										false,
                                        reducedNetFilePath,
                                        false,
                                        true
								),
								transformedModel,
								clonedQuery
						);
					}

					if (overapprox_result.getQueryResult() != null) {
						if (!overapprox_result.error() && model.isUntimed() || (
								(query.queryType() == QueryType.EF && !overapprox_result.getQueryResult().isQuerySatisfied()) ||
										(query.queryType() == QueryType.AG && overapprox_result.getQueryResult().isQuerySatisfied()))
						) {
							VerificationResult<TAPNNetworkTrace> value = new VerificationResult<TAPNNetworkTrace>(
									overapprox_result.getQueryResult(),
									decomposeTrace(overapprox_result.getTrace(), transformedModel.value2()),
									overapprox_result.verificationTime(),
									overapprox_result.stats(),
									true
							);
							value.setNameMapping(transformedModel.value2());
							return value;
						}
					}
				}
			}
		}
		
		ApproximationWorker worker = new ApproximationWorker();
		
		return worker.normalWorker(options, modelChecker, transformedModel, composer, clonedQuery, this, model);
	}

	private TAPNNetworkTrace decomposeTrace(TimedArcPetriNetTrace trace, NameMapping mapping) {
		if (trace == null) {
			return null;
		}

		TAPNTraceDecomposer decomposer = new TAPNTraceDecomposer(trace, model, mapping);
		return decomposer.decompose();
	}

	private void MapQueryToNewNames(TAPNQuery query, NameMapping mapping) {
		RenameAllPlacesVisitor placeVisitor = new RenameAllPlacesVisitor(mapping);
		RenameAllTransitionsVisitor transitionVisitor = new RenameAllTransitionsVisitor(mapping);
		query.getProperty().accept(placeVisitor, null);
		query.getProperty().accept(transitionVisitor, null);
	}

	@Override
	protected void done() {
		if (!isCancelled()) {
			VerificationResult<TAPNNetworkTrace> result = null;

			try {
				result = get();
			} catch (InterruptedException e) {
				e.printStackTrace();
				showErrorMessage(e.getMessage());
				return;
			} catch (ExecutionException e) {
				if(!(e.getCause() instanceof UnsupportedModelException)){
					e.printStackTrace();
				}
				showErrorMessage(e.getMessage());
				return;
			}
			firePropertyChange("state", StateValue.PENDING, StateValue.DONE);

			if (showResult(result) && spinner != null) {
			    options = new VerifyPNOptions(options.extraTokens(), pipe.dataLayer.TAPNQuery.TraceOption.NONE, SearchOption.BFS, false, ModelReduction.BOUNDPRESERVING, false, false, 1, pipe.dataLayer.TAPNQuery.QueryCategory.Default, pipe.dataLayer.TAPNQuery.AlgorithmOption.CERTAIN_ZERO, false, pipe.dataLayer.TAPNQuery.QueryReductionTime.NoTime, false, null, false, false);
                KBoundAnalyzer optimizer = new KBoundAnalyzer(model, options.extraTokens(), modelChecker, new MessengerImpl(), spinner);
                optimizer.analyze((VerifyTAPNOptions) options, true);
            }
		} else {
			modelChecker.kill();
			messenger.displayInfoMessage("Verification was interrupted by the user. No result found!", "Verification Cancelled");

		}
	}

	private String error;
	private void showErrorMessage(String errorMessage) {
		error = errorMessage;
		//The invoke later will make sure all the verification is finished before showing the error
		SwingUtilities.invokeLater(() -> {
			messenger.displayErrorMessage("The engine selected in the query dialog cannot verify this model.\nPlease choose another engine.\n" + error);
			CreateGui.getCurrentTab().editSelectedQuery();
		});
	}

	protected abstract boolean showResult(VerificationResult<TAPNNetworkTrace> result);
}
