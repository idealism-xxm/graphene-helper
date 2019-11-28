package cn.idealismxxm.graphenehelper.generation.action;

import cn.idealismxxm.graphenehelper.generation.handler.GenerateMutationHandler;

public class GenerateMutationAction extends BaseGenerateAction {

    public GenerateMutationAction() {
        super(new GenerateMutationHandler());
    }
}
