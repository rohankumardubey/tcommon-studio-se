// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package tisstudio.metadata.sap;

import junit.framework.Assert;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.talend.swtbot.TalendSwtBotForTos;

/**
 * DOC Administrator class global comment. Detailled comment
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class DeleteSapConnectionTest extends TalendSwtBotForTos {

    private SWTBotTree tree;

    private SWTBotShell shell;

    private SWTBotView view;

    private static String SAPNAME = "sap1"; //$NON-NLS-1$

    private static String CLIENT = "000"; //$NON-NLS-1$

    private static String HOST = "192.168.0.185"; //$NON-NLS-1$

    private static String USER = "TALEND"; //$NON-NLS-1$

    private static String PASSWORD = "FRANCE"; //$NON-NLS-1$

    private static String SYSTEM_NUMBER = "00"; //$NON-NLS-1$

    private static String LANGUAGE = "en"; //$NON-NLS-1$

    @Before
    public void InitialisePrivateFields() {
        view = gefBot.viewByTitle("Repository");
        view.setFocus();
        tree = new SWTBotTree((Tree) gefBot.widget(WidgetOfType.widgetOfType(Tree.class), view.getWidget()));
        tree.setFocus();

        tree.expandNode("Metadata").getNode("SAP Connections").contextMenu("Create SAP connection").click();
        gefBot.waitUntil(Conditions.shellIsActive("SAP Connection"));
        gefBot.shell("SAP Connection").activate();

        /* step 1 of 2 */
        gefBot.textWithLabel("Name").setText(SAPNAME);
        gefBot.button("Next >").click();

        /* step 2 of 2 */
        gefBot.textWithLabel("Client").setText(CLIENT);
        gefBot.textWithLabel("Host").setText(HOST);
        gefBot.textWithLabel("User").setText(USER);
        gefBot.textWithLabel("Password").setText(PASSWORD);
        gefBot.textWithLabel("System Number").setText(SYSTEM_NUMBER);
        gefBot.textWithLabel("Language").setText(LANGUAGE);
        gefBot.button("Check").click();
        // shell = gefBot.shell("Check SAP Connection ");
        // shell.activate();
        // gefBot.waitUntil(Conditions.shellIsActive("Check SAP Connection "));
        gefBot.waitUntil(new DefaultCondition() {

            public boolean test() throws Exception {
                return gefBot.shell("Check SAP Connection ").isActive();
            }

            public String getFailureMessage() {
                gefBot.shell("SAP Connection").close();
                return "connection failure";
            }
        });
        gefBot.button("OK").click();
        gefBot.waitUntil(Conditions.shellCloses(shell));

        gefBot.button("Finish").click();
    }

    @Test
    public void deleteSapConnection() {
        tree.expandNode("Metadata", "SAP Connections").getNode(SAPNAME + " 0.1").contextMenu("Delete").click();

        SWTBotTreeItem newSapItem = tree.expandNode("Recycle bin").select(SAPNAME + " 0.1" + " ()");
        Assert.assertNotNull(newSapItem);
    }

    @After
    public void removePreviouslyCreateItems() {
        tree.select("Recycle bin").contextMenu("Empty recycle bin").click();
        gefBot.waitUntil(Conditions.shellIsActive("Empty recycle bin"));
        gefBot.button("Yes").click();
    }
}
