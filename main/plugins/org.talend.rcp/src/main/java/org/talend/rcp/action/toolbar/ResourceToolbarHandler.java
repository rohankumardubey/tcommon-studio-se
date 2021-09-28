package org.talend.rcp.action.toolbar;

import org.apache.commons.codec.binary.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.program.Program;
import org.eclipse.ui.IWorkbenchWindow;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.service.IExchangeService;
import org.talend.core.service.ITutorialsService;

public class ResourceToolbarHandler extends AbstractHandler {

    public static final String LEARN = "Learn"; //$NON-NLS-1$

    public static final String LEARN_ORIG_URL = "https://help.talend.com";//$NON-NLS-1$

    public static final String ASK = "Ask"; //$NON-NLS-1$

    public static final String ASK_ORIG_URL = "https://community.talend.com";//$NON-NLS-1$

    public static final String EXCHANGE = "Exchange"; //$NON-NLS-1$

    public static final String EXCHANGE_ORIG_URL = "http://www.talendforge.org/exchange/index.php";//$NON-NLS-1$

    public static final String VIDEOS = "Videos"; //$NON-NLS-1$

    public static final String VIDEOS_ORIG_URL = "https://www.talendforge.org/tutorials";//$NON-NLS-1$

    public static final String CLOUD = "Cloud"; //$NON-NLS-1$

    public static final String CLOUD_ORIG_URL = "https://iam.integrationcloud.talend.com/idp/trial-registration?utm_medium=studio&utm_source=toolbar&utm_campaign=dynamic_acronym";//$NON-NLS-1$

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String id = event.getCommand().getId();
        if (StringUtils.equals(id, "org.talend.resoruses.toolbar.Learn")) {//$NON-NLS-1$
            openBrower(LEARN_ORIG_URL);
        }
        if (StringUtils.equals(id, "org.talend.resoruses.toolbar.Ask")) {//$NON-NLS-1$
            openBrower(ASK_ORIG_URL);
        }
        if (StringUtils.equals(id, "org.talend.resoruses.toolbar.Exchange")) {//$NON-NLS-1$
            IExchangeService service = GlobalServiceRegister.getDefault().getService(IExchangeService.class);
            service.openExchangeEditor();
        }
        if (StringUtils.equals(id, "org.talend.resoruses.toolbar.Vidoes")) {//$NON-NLS-1$
            ITutorialsService service = GlobalServiceRegister.getDefault().getService(ITutorialsService.class);
            service.openTutorialsDialog();
        }
        if (StringUtils.equals(id, "org.talend.resoruses.toolbar.Cloud")) {//$NON-NLS-1$
            openBrower(CLOUD_ORIG_URL);
        }
        return null;
    }

    protected void openBrower(String url) {
        Program.launch(url);
    }
}