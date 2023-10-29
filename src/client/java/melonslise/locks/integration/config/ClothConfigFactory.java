package melonslise.locks.integration.config;

@Deprecated
public class ClothConfigFactory
{
	/*
	private static final Map<Class<?>, TriConsumer<ConfigScanner.Entry, String, ConfigEntryBuilder>> CONVERTERS = Map.of(
		boolean.class, (definition, modId, builder) ->
			builder.startBooleanToggle(Text.translatable(modId + ".config.entry." + definition.name()), (Boolean) definition.getter().get()).build(),
		int.class, (definition, modId, builder) ->
	);

	public static Screen create(Class<?> c, String modId, Identifier background, Screen parent)
	{
		ConfigBuilder configBuilder = ConfigBuilder.create()
			.setTitle(Text.translatable(modId + ".config.title"))
			.setDefaultBackgroundTexture(background)
			.setParentScreen(parent);

		ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

		ConfigCategory main = configBuilder.getOrCreateCategory(configBuilder.getTitle());

		Map<String, SubCategoryBuilder> categories = new HashMap<>();

		for(var definition : ConfigScanner.entries(c))
		{
			if(definition.category() != null)
			{
				SubCategoryBuilder categoryBuilder = categories.computeIfAbsent(definition.category(), k ->
					entryBuilder.startSubCategory(Text.translatable(modId + ".config.category." + k)))
					.setExpanded(true);
			}
		}

		return configBuilder.build();
	}
	 */

	/*
	public static Screen create(Screen parent)
	{
		ConfigBuilder config = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Text.translatable("locks.config.title"));

		ConfigEntryBuilder info = config.entryBuilder();

		config.getOrCreateCategory(Text.translatable("locks.config.category.main"))
			.addEntry(subCategory(info.startSubCategory(Text.translatable("subcat")),
				info.startBooleanToggle(Text.translatable("opt"), true)));

		return config.build();

		return YetAnotherConfigLib.createBuilder()
			.title(Text.translatable(""))
			.category(ConfigCategory.createBuilder()
				.name(Text.translatable("cat"))
				.group(OptionGroup.createBuilder()
					.name(Text.translatable("grp"))
					.option(Option.<Boolean>createBuilder()
						.name(Text.translatable("opt"))
						.binding(true, () -> true, value -> {})
						.controller(TickBoxControllerBuilder::create)
						.build())
					.build())
				.build())
			.build()
			.generateScreen(parent);
	}

	private static SubCategoryListEntry subCategory(SubCategoryBuilder subCategory, AbstractFieldBuilder... entries)
	{
		subCategory.setExpanded(true);

		for (var entry : entries)
		{
			subCategory.add(entry.build());
		}

		return subCategory.build();
	}
	*/
}