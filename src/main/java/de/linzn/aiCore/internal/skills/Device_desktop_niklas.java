package de.linzn.aiCore.internal.skills;

import de.linzn.aiCore.App;
import de.linzn.aiCore.internal.IObjectClass;
import de.linzn.aiCore.internal.Reflector;
import de.linzn.aiCore.internal.container.ClientContainer;
import de.linzn.aiCore.internal.container.KeywordContainer;
import de.linzn.aiCore.internal.container.ObjectContainer;
import de.linzn.aiCore.internal.container.ResultContainer;
import de.linzn.aiCore.processing.network.writeBack.SendNotification;

public class Device_desktop_niklas implements IObjectClass {
    private ClientContainer clientContainer;
    private ObjectContainer objectcontainer;
    private KeywordContainer keywordContainer;

    @Override
    public void initial(ClientContainer clientCon, ObjectContainer objectCon, KeywordContainer keywordCon) {
        this.clientContainer = clientCon;
        this.objectcontainer = objectCon;
        this.keywordContainer = keywordCon;
    }

    @Override
    public void runTask(String function) {
        new Reflector().functionRunner(this, function);
    }

    @Override
    public void resultTask() {
        ResultContainer resultCon = App.appInstance.mysqlData.dbresult.getResultByObjects(objectcontainer, keywordContainer);
        if (resultCon == null) {
            App.logger("No ResultContainer found");
        } else {
            this.clientContainer.sendResult(resultCon.result);
            new SendNotification().sendNotification(resultCon.result);
        }
    }


    public void wakeup() {
        if (App.appInstance.skillApi.powerControl.wakeOnLan("device_desktop_niklas")) {
            this.resultTask();
        }
    }

}
