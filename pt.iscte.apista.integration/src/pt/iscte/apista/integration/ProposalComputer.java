package pt.iscte.apista.integration;

import pt.iscte.apista.core.SystemConfiguration;
import pt.iscte.apista.eclipse.ApistaProposalComputer;
import pt.iscte.apista.ngram.models.NGramModel;

public class ProposalComputer extends ApistaProposalComputer{

	
	public ProposalComputer() {
		super(NGramModel.class,new SystemConfiguration("C:/Users/Gonçalo/Dropbox/Thesis/Code/pt.iscte.apista.evaluationsystem/resources/config.properties"));
	}

}
