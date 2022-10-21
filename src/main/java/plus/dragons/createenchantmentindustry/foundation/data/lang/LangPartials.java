package plus.dragons.createenchantmentindustry.foundation.data.lang;

import com.google.common.base.Supplier;
import com.google.gson.JsonElement;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.simibubi.create.foundation.utility.Lang;
import plus.dragons.createenchantmentindustry.EnchantmentIndustry;
import plus.dragons.createenchantmentindustry.foundation.data.advancement.CeiAdvancements;

/*
MIT License

Copyright (c) 2019 simibubi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

public enum LangPartials {
	ADVANCEMENTS("Advancements", CeiAdvancements::provideLangEntries),
	INTERFACE("UI & Messages"),
	TOOLTIPS("Item Descriptions"),
	PONDER("Ponder Content");

	private final String display;
	private final Supplier<JsonElement> provider;

	LangPartials(String display) {
		this.display = display;
		this.provider = this::fromResource;
	}

	LangPartials(String display, Supplier<JsonElement> customProvider) {
		this.display = display;
		this.provider = customProvider;
	}

	public String getDisplay() {
		return display;
	}

	public JsonElement provide() {
		return provider.get();
	}

	private JsonElement fromResource() {
		String fileName = Lang.asId(name());
		String filepath = "assets/" + EnchantmentIndustry.ID + "/lang/partial/" + fileName + ".json";
		JsonElement element = FilesHelper.loadJsonResource(filepath);
		if (element == null)
			throw new IllegalStateException(String.format("Could not find default lang file: %s", filepath));
		return element;
	}

}
