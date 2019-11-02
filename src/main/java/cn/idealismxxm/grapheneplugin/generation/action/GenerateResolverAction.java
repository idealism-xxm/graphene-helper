package cn.idealismxxm.grapheneplugin.generation.action;

import cn.idealismxxm.grapheneplugin.generation.handler.GenerateResolverHandler;

public class GenerateResolverAction extends BaseGenerateAction {

    public GenerateResolverAction() {
        super(new GenerateResolverHandler());
    }
}
