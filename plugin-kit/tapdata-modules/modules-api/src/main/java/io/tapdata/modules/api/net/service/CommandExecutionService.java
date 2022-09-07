package io.tapdata.modules.api.net.service;


import io.tapdata.pdk.apis.entity.CommandInfo;

import java.util.Map;
import java.util.function.BiConsumer;

public interface CommandExecutionService {
	void call(CommandInfo commandInfo, BiConsumer<Map<String, Object>, Throwable> biConsumer);
}
