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
package tosstudio.routines;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.talend.swtbot.TalendSwtBotForTos;

/**
 * DOC Administrator class global comment. Detailled comment
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CopyPasteRoutineTest extends TalendSwtBotForTos {

    private SWTBotTree tree;

    private SWTBotShell shell;

    private SWTBotView view;

    private static String ROUTINENAME = "routine1"; //$NON-NLS-1$

    @Before
    public void InitialisePrivateFields() {
        view = gefBot.viewByTitle("Repository");
        view.setFocus();
        tree = new SWTBotTree((Tree) gefBot.widget(WidgetOfType.widgetOfType(Tree.class), view.getWidget()));
        tree.setFocus();
        tree.expandNode("Code").getNode("Routines").contextMenu("Create routine").click();

        gefBot.waitUntil(Conditions.shellIsActive("New routine"));
        shell = gefBot.shell("New routine");
        shell.activate();

        gefBot.textWithLabel("Name").setText(ROUTINENAME);

        gefBot.button("Finish").click();
    }

    @Test
    public void copyAndPasteRoutine() {
        tree.expandNode("Code", "Routines").getNode(ROUTINENAME + " 0.1").contextMenu("Copy").click();
        tree.expandNode("Code", "Routines").contextMenu("Paste").click();

        SWTBotTreeItem newRoutineItem = tree.expandNode("Code", "Routines").select("Copy_of_" + ROUTINENAME + " 0.1");
        Assert.assertNotNull(newRoutineItem);
    }

    @After
    public void removePreviouslyCreateItems() {
        gefBot.cTabItem(ROUTINENAME + " 0.1").close();
        tree.expandNode("Code", "Routines").getNode(ROUTINENAME + " 0.1").contextMenu("Delete").click();
        tree.expandNode("Code", "Routines").getNode("Copy_of_" + ROUTINENAME + " 0.1").contextMenu("Delete").click();

        tree.select("Recycle bin").contextMenu("Empty recycle bin").click();
        gefBot.waitUntil(Conditions.shellIsActive("Empty recycle bin"));
        gefBot.button("Yes").click();
    }
}
