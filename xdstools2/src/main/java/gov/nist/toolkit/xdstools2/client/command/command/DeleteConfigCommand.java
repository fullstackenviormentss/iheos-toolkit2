package gov.nist.toolkit.xdstools2.client.command.command;

import gov.nist.toolkit.xdstools2.client.util.ClientUtils;
import gov.nist.toolkit.xdstools2.shared.command.request.SimConfigRequest;

/**
 * Created by onh2 on 11/7/16.
 */
public abstract class DeleteConfigCommand extends GenericCommand<SimConfigRequest,String>{
    @Override
    public void run(SimConfigRequest var1) {
        ClientUtils.INSTANCE.getToolkitServices().deleteConfig(var1,this);
    }
}
