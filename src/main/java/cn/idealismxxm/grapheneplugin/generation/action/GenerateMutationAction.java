package cn.idealismxxm.grapheneplugin.generation.action;

import cn.idealismxxm.grapheneplugin.generation.handler.GenerateMutationHandler;

public class GenerateMutationAction extends BaseGenerateAction {

    public GenerateMutationAction() {
        super(new GenerateMutationHandler());
    }
}
