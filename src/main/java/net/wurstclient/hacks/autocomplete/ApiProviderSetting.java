/*
 * Copyright (c) 2014-2023 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks.autocomplete;

import net.wurstclient.settings.EnumSetting;

public final class ApiProviderSetting
        extends EnumSetting<ApiProviderSetting.ApiProvider> {
    public ApiProviderSetting() {
        super("API提供者",
                "\u00a7lOpenAI\u00a7r允许您使用ChatGPT等模型，但需要具有API访问权限的帐户，使用它需要付费，并且会将您的聊天历史发送到他们的服务器。这个名字是个谎言 - 它是闭源的。\n\n"
                        + "\u00a7loobabooga\u00a7r允许您使用LLaMA和许多其他模型。它是OpenAI的真正开源替代品，可以在您自己的计算机上本地运行。它是免费使用的，不会将您的聊天历史发送到任何服务器。",
                ApiProvider.values(), ApiProvider.OOBABOOGA);

    }

    public enum ApiProvider {
        OPENAI("OpenAI"),
        OOBABOOGA("oobabooga");

        private final String name;

        private ApiProvider(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
