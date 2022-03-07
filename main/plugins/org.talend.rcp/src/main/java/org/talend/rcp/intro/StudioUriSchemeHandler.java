package org.talend.rcp.intro;

import org.eclipse.urischeme.IUriSchemeHandler;

public class StudioUriSchemeHandler implements IUriSchemeHandler{

    @Override
    public void handle(String uri) {
       System.out.println(uri);
        
    }

}
