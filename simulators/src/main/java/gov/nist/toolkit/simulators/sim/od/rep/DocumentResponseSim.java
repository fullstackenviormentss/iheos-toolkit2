package gov.nist.toolkit.simulators.sim.od.rep;

import gov.nist.toolkit.errorrecording.ErrorRecorder;
import gov.nist.toolkit.errorrecording.client.XdsErrorCode.Code;
import gov.nist.toolkit.registrymsg.registry.Response;
import gov.nist.toolkit.registrysupport.MetadataSupport;
import gov.nist.toolkit.simulators.sim.reg.RegistryResponseGeneratingSim;
import gov.nist.toolkit.simulators.sim.rep.RepIndex;
import gov.nist.toolkit.simulators.support.DsSimCommon;
import gov.nist.toolkit.simulators.support.SimCommon;
import gov.nist.toolkit.simulators.support.StoredDocument;
import gov.nist.toolkit.simulators.support.TransactionSimulator;
import gov.nist.toolkit.valregmsg.registry.RetrieveMultipleResponse;
import gov.nist.toolkit.valsupport.client.ValidationContext;
import gov.nist.toolkit.valsupport.engine.MessageValidatorEngine;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DocumentResponseSim extends TransactionSimulator implements RegistryResponseGeneratingSim{
	DsSimCommon dsSimCommon;
	List<String> documentUids;
	List<String> dynamicDocumentUuids = new ArrayList<String>();
	RetrieveMultipleResponse response;
	RepIndex repIndex;
	String repositoryUniqueId;

	public DocumentResponseSim(ValidationContext vc, List<String> documentUids, SimCommon common, DsSimCommon dsSimCommon, String repositoryUniqueId) {
		super(common, null);
		this.dsSimCommon = dsSimCommon;
		this.documentUids = documentUids;
		this.repIndex = dsSimCommon.repIndex;
		this.repositoryUniqueId = repositoryUniqueId;
	}

	public void run(ErrorRecorder er, MessageValidatorEngine mvc) {
		try {
			response = new RetrieveMultipleResponse();

			// At this point, there are no documents in this repository, so insert a fake one here.
			// Begin insert a fake document here
			String dynamicDocumentUuid = "urn:uuid:od-doc_private-uid-1";
			StoredDocument storedDocument = repIndex.getDocumentCollection().getStoredDocument(dynamicDocumentUuid);
			if (storedDocument==null) {
				storedDocument =  new StoredDocument("nonexistent-od-file-path",dynamicDocumentUuid);
				storedDocument.setContent("This content is served on-demand.".getBytes());
				storedDocument.setMimetype("text/plain");

				repIndex.getDocumentCollection().add(storedDocument);
			}
			// End

			// Replace all OD promise Uuids with fake dynamic contents
			for (String uid : documentUids) {
				dynamicDocumentUuids.add(dynamicDocumentUuid);
			}

			dsSimCommon.addDocumentAttachments(dynamicDocumentUuids, er);
			// End On-Demand update block

			Collection<StoredDocument> documents = dsSimCommon.getAttachments();

			OMElement root = response.getRoot();

			for (StoredDocument document : documents) {
				String uid = document.getUid();

				StoredDocument sd = repIndex.getDocumentCollection().getStoredDocument(uid);

				OMElement docResponse = MetadataSupport.om_factory.createOMElement(MetadataSupport.document_response_qnamens);

				OMElement repId = MetadataSupport.om_factory.createOMElement(MetadataSupport.repository_unique_id_qnamens);
				repId.setText(repositoryUniqueId);
				docResponse.addChild(repId);

				OMElement docId = MetadataSupport.om_factory.createOMElement(MetadataSupport.document_unique_id_qnamens);
				docId.setText(sd.getUid());
				docResponse.addChild(docId);

				// Begin On-Demand
				OMElement newDocId = MetadataSupport.om_factory.createOMElement(MetadataSupport.newDocumentUniqueId);
				newDocId.setText(sd.getUid() + ".1"); // TODO: Find out what needs to go here.
				docResponse.addChild(newDocId);

				// TODO: Find out if the NewRepositoryUniqueId needs to be added in. (Vol. 2b, 3.43.5.1.3).
				// A: Not required without the Persistence option. (Bill)
				// End On-Demand

				OMElement mimeType = MetadataSupport.om_factory.createOMElement(MetadataSupport.mimetype_qnamens);
				mimeType.setText(sd.getMimeType());
				docResponse.addChild(mimeType);

				OMElement doc = MetadataSupport.om_factory.createOMElement(MetadataSupport.document_qnamens);
				docResponse.addChild(doc);

				OMElement include = MetadataSupport.om_factory.createOMElement(MetadataSupport.xop_include_qnamens);
				OMAttribute href = MetadataSupport.om_factory.createOMAttribute("href", null, "cid:" + document.cid);
				include.addAttribute(href);
				doc.addChild(include);

				root.addChild(docResponse);
			}
		}
		catch (Exception e) {
			er.err(Code.XDSRepositoryError, e);
			return;
		}
	}

	public Response getResponse() {
		return response;
	}

}