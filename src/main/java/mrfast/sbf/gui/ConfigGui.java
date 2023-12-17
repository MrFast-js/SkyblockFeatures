package mrfast.sbf.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import mrfast.sbf.core.ConfigManager;
import mrfast.sbf.core.ConfigManager.PropertyType;
import mrfast.sbf.core.ConfigManager.Property;
import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.ScrollComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIImage;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.UIWrappedText;
import gg.essential.elementa.components.inspector.Inspector;
import gg.essential.elementa.constraints.*;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.effects.OutlineEffect;
import gg.essential.elementa.effects.RecursiveFadeEffect;
import gg.essential.elementa.effects.ScissorEffect;
import gg.essential.vigilance.gui.VigilancePalette;
import gg.essential.vigilance.gui.common.input.UITextInput;
import gg.essential.vigilance.gui.common.shadow.ShadowIcon;
import gg.essential.vigilance.gui.settings.*;
import gg.essential.vigilance.utils.ResourceImageFactory;
import kotlin.Unit;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.Config;
import mrfast.sbf.utils.GuiUtils;
import mrfast.sbf.utils.Utils;

public class ConfigGui extends WindowScreen {
    public static SortedMap<String, SortedMap<String,List<Property>>> categories = new TreeMap<>();
    public static HashMap<String,Boolean> expandedFeatures = new HashMap<>();
    public static HashMap<Property, Object> valueMap = new HashMap<>();
    public static String selectedCategory = "General";
    public String searchQuery = "";
    static Boolean furfSkyThemed = false;
    static boolean quickSwapping = false;

    @Override
	public void onScreenClose() {
        ConfigManager.saveConfig(SkyblockFeatures.config);
        if(quickSwapping) {
            quickSwapping = false;
        } else {
            Utils.GetMC().gameSettings.guiScale = GuiUtils.lastGuiScale;
        }
	}

    // Text/Lines colors
    Color titleColor = SkyblockFeatures.config.titleColor;//new Color(0x00FFFF);
    Color guiLines = SkyblockFeatures.config.guiLines;
    Color selectedCategoryColor = SkyblockFeatures.config.selectedCategory;
    Color hoveredCategory = SkyblockFeatures.config.hoveredCategory;
    Color defaultCategory = SkyblockFeatures.config.defaultCategory;
    Color versionText = SkyblockFeatures.config.versionColor;//new Color(0xFFFFFF);
    Color editGuiText = SkyblockFeatures.config.editGuiText;
    Color featureDescription = SkyblockFeatures.config.featureDescription;

    // Background colors
    Color mainBackground = SkyblockFeatures.config.mainBackground;
    Color searchBoxBackground = SkyblockFeatures.config.searchBoxBackground;

    Color clear = new Color(0,0,0,0);
    public ConfigGui(Boolean doAnimation) {
        super(ElementaVersion.V2);

        // init && not quickswap
        if(doAnimation) {
            GuiUtils.saveGuiScale();
        }

        reloadAllCategories();
        furfSkyThemed = SkyblockFeatures.config.furfSkyThemed;
        int screenHeight = Utils.GetMC().displayHeight/2;
        UIComponent box = new UIRoundedRectangle(10f)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .setWidth(new RelativeConstraint(0.70f))
            .setHeight(new RelativeConstraint(0.70f))
            .setChildOf(getWindow())
            .setColor(mainBackground)
            .enableEffect(new ScissorEffect());

        String outlinePath = furfSkyThemed?"/assets/skyblockfeatures/gui/largeOutlineFurf.png":"/assets/skyblockfeatures/gui/largeOutline.png";
        UIImage.ofResourceCached(outlinePath).setChildOf(box)
            .setX(new PixelConstraint(0f))
            .setY(new PixelConstraint(0f))
            .setWidth(new RelativeConstraint(1f))
            .setHeight(new RelativeConstraint(1f));
        float guiWidth = box.getWidth();
        float guiHeight = box.getHeight();
        double fontScale = screenHeight/540d;

        UIComponent titleArea = new UIBlock().setColor(clear).setChildOf(box)
            .setX(new CenterConstraint())
            .setWidth(new PixelConstraint(guiWidth))
            .setHeight(new PixelConstraint(0.15f*guiHeight))
            .enableEffect(new ScissorEffect());
        // Title text
        UIComponent titleText = new UIText("Skyblock Features")
            .setColor(titleColor)
            .setChildOf(titleArea)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .enableEffect(new ScissorEffect())
            .setTextScale(new PixelConstraint((float) (doAnimation?1*fontScale:4*fontScale)));

        new UIText("v"+SkyblockFeatures.VERSION)
            .setColor(versionText)
            .setChildOf(titleArea)
            .setX(new RelativeConstraint(0.77f))
            .setY(new RelativeConstraint(0.7f))
            .enableEffect(new ScissorEffect())
            .setTextScale(new PixelConstraint((float) fontScale));

        if(Utils.isDeveloper() && SkyblockFeatures.config.showInspector) {
             new Inspector(getWindow()).setChildOf(getWindow());
        }

        UIComponent searchBox = new UIRoundedRectangle(5f)
            .setChildOf(titleArea)
            .setX(new PixelConstraint(guiWidth-90))
            .setY(new CenterConstraint())
            .setWidth(new PixelConstraint(80))
            .setColor(searchBoxBackground)
            .setHeight(new PixelConstraint(15f));

        UITextInput input = (UITextInput) new UITextInput("Search")
            .setChildOf(searchBox)
            .setX(new PixelConstraint(5f))
            .setWidth(new PixelConstraint(80))
            .setHeight(new PixelConstraint(15f))
            .setY(new PixelConstraint(3f));

        titleArea.onMouseClickConsumer((event)->{
            input.grabWindowFocus();
        });

        // Gray horizontal line 1px from bottom of the title area
        new UIBlock().setChildOf(titleArea)
            .setWidth(new PixelConstraint(guiWidth-2))
            .setHeight(new PixelConstraint(1f))
            .setX(new CenterConstraint())
            .setY(new PixelConstraint(titleArea.getHeight()-1))
            .setColor(guiLines);

        // Area of where the currently selected categorie's feature will be displayed
        UIComponent loadedFeaturesList = new ScrollComponent("No Matching Settings Found", 10f, searchBoxBackground, false, true, false, false, 25f, 1f, null)
            .setX(new PixelConstraint(0.25f*guiWidth))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .enableEffect(new ScissorEffect())
            .setWidth(new PixelConstraint(0.75f*guiWidth))
            .setHeight(new PixelConstraint(((0.85f*guiHeight)-(furfSkyThemed?6:1))));

        loadedFeaturesList.clearChildren();
        reloadFeatures(loadedFeaturesList,guiHeight,guiWidth,fontScale);

        // Search box typing
        input.onKeyType((component, character, integer) -> {
            // Set the search query
            searchQuery = ((UITextInput) component).getText().toLowerCase();
            // Clear all features and children
            loadedFeaturesList.clearChildren();
            // Reload features
            reloadFeatures(loadedFeaturesList,guiHeight,guiWidth,fontScale);
            return Unit.INSTANCE;
        });

        // Side bar on the left that holds the categories
        UIComponent sidebarArea = new UIBlock()
            .setX(new PixelConstraint(-5f))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setWidth(new PixelConstraint(0.25f*guiWidth))
            .setHeight(new PixelConstraint(0.85f*guiHeight))
            .setColor(clear)
            .enableEffect(new ScissorEffect());

        UIComponent sidebarAreaScroll = new ScrollComponent("No Matching Settings Found", 10f, searchBoxBackground, false, true, false, false, 25f, 1f, null)
            .setX(new PixelConstraint(0f))
            .setY(new PixelConstraint(0f))
            .setWidth(new RelativeConstraint(1f))
            .setHeight(new RelativeConstraint(1f))
            .setChildOf(sidebarArea)
            .setColor(clear)
            .enableEffect(new ScissorEffect());
        // Seperator to the right side of the sidebar
        UIComponent sidebarSeperator = new UIBlock()
            .setWidth(new PixelConstraint(1f))
            .setHeight(new PixelConstraint((0.85f*guiHeight)-1))
            .setX(new PixelConstraint(0.25f*guiWidth))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setColor(guiLines);

        // Draw categorys on sidebar
        for(String categoryName:categories.keySet()) {
            if(categoryName.contains("Developer") && !Utils.isDeveloper()) continue;

            UIComponent ExampleCategory = new UIText(categoryName)
                .setChildOf(sidebarAreaScroll)
                .setColor(defaultCategory)
                .setX(new CenterConstraint())
                .setY(new SiblingConstraint((float) (2.5f*fontScale)))
                .enableEffect(new RecursiveFadeEffect())
                .setTextScale(new PixelConstraint((float) fontScale*1.5f));

            // Set color of selected category
            if(categoryName.equals(selectedCategory)) {
                ExampleCategory.setColor(selectedCategoryColor);
            }
            ExampleCategory.onMouseEnterRunnable(()->{
                if(!categoryName.equals(selectedCategory)) {
                    AnimatingConstraints anim = ExampleCategory.makeAnimation();
                    anim.setColorAnimation(Animations.OUT_EXP,0.5f,new ConstantColorConstraint(hoveredCategory));
                    ExampleCategory.animateTo(anim);
                }
            });
            ExampleCategory.onMouseLeaveRunnable(()->{
                if(!categoryName.equals(selectedCategory)) {
                    AnimatingConstraints anim = ExampleCategory.makeAnimation();
                    anim.setColorAnimation(Animations.OUT_EXP,0.5f,new ConstantColorConstraint(defaultCategory));
                    ExampleCategory.animateTo(anim);
                }
            });
            ExampleCategory.onMouseClickConsumer((event)->{
                if(selectedCategory.equals(categoryName)) return;
                LoadCategory(categoryName);
            });
        }

        UIComponent editGuiButton;

        // Check if the image should be used or not
        if (furfSkyThemed) {
            editGuiButton = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/button.png", true), false)
                    .setChildOf(sidebarArea)
                    .setX(new RelativeConstraint(0.04f))
                    .setWidth(new PixelConstraint(118f))
                    .setHeight(new PixelConstraint(32f))
                    .setY(new RelativeConstraint(0.88f));
        } else {
            editGuiButton = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/default_button.png", true), false)
                    .setChildOf(sidebarArea)
                    .setX(new RelativeConstraint(0.04f))
                    .setWidth(new PixelConstraint(118f))
                    .setHeight(new PixelConstraint(32f))
                    .setY(new RelativeConstraint(0.88f));
        }

        new UIText("Edit Gui Locations").setColor(editGuiText).setChildOf(editGuiButton)
            .setTextScale(new PixelConstraint((float) fontScale))
            .setX(new CenterConstraint())
            .setY(new CenterConstraint());

        // Open gui locations gui when clicked
        editGuiButton.onMouseClickConsumer((event)->{
            GuiUtils.openGui(new EditLocationsGui());
        });
        UIComponent discordButton = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/discord.png",true),true)
            .setX(new RelativeConstraint(0.025f))
            .setWidth(new PixelConstraint(30))
            .setHeight(new PixelConstraint(30))
            .setChildOf(titleArea)
            .setY(new CenterConstraint());
        discordButton.onMouseClickConsumer((event)->{try {Desktop.getDesktop().browse(new URI("https://discord.gg/MDTEAjbNw8"));} catch (Exception e) {e.printStackTrace();}});

        UIComponent githubButton = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/github.png",true),true)
            .setX(new SiblingConstraint(12f))
            .setWidth(new PixelConstraint(26))
            .setHeight(new PixelConstraint(26))
            .setChildOf(titleArea)
            .setY(new CenterConstraint());
        githubButton.onMouseClickConsumer((event)->{try {Desktop.getDesktop().browse(new URI("https://github.com/MrFast-js/SkyblockFeatures"));} catch (Exception e) {e.printStackTrace();}});

        box.addChild(titleArea);
        box.addChild(sidebarArea);
        box.addChild(sidebarSeperator);
        box.addChild(loadedFeaturesList);
        box.addChild(editGuiButton);

        UIComponent sidebarContainer = new UIBlock(clear)
                .setX(new PixelConstraint(3f,true))
                .setY(new PixelConstraint(titleArea.getHeight()))
                .setChildOf(box)
                .setWidth(new PixelConstraint(5f))
                .setHeight(new PixelConstraint(((0.85f*guiHeight)-(furfSkyThemed?6:1))));

        UIComponent scrollbar = new UIRoundedRectangle(3f)
                .setColor(new Color(200,200,200,200))
                .setChildOf(sidebarContainer)
                .setWidth(new PixelConstraint(5f))
                .setX(new PixelConstraint(0f))
                .setHeight(new RelativeConstraint(1f));

        ((ScrollComponent) loadedFeaturesList).setVerticalScrollBarComponent(scrollbar,true);

        if(doAnimation) {
            box.setWidth(new PixelConstraint(0f));

            AnimatingConstraints anim = box.makeAnimation();
            anim.setWidthAnimation(Animations.OUT_EXP, 0.5f, new RelativeConstraint(0.70f));
            box.animateTo(anim);

            AnimatingConstraints animation = titleText.makeAnimation();
            animation.setTextScaleAnimation(Animations.OUT_EXP, 0.5f, new PixelConstraint((float) (4.0*fontScale)));
            titleText.animateTo(animation);
        }
    }

    public void reloadAllCategories() {
        categories.clear();
        Config field = SkyblockFeatures.config;
        Field[] fieldsOfFieldClass = Config.class.getFields();
        for (Field ofFieldClass : fieldsOfFieldClass) {
            try {
                Object value = ofFieldClass.get(field);
                if (ofFieldClass.isAnnotationPresent(Property.class)) {
                    Property feature = ofFieldClass.getAnnotation(Property.class);
                    // Create category if not exist already
                    if (!categories.containsKey(feature.category())) {
                        categories.put(feature.category(), new TreeMap<>());
                    }
                    SortedMap<String, List<Property>> category = categories.get(feature.category());

                    // Create subcategory if not exist already
                    if (!category.containsKey(feature.subcategory())) {
                        category.put(feature.subcategory(), new ArrayList<>());
                    }
                    List<Property> subcategory = category.get(feature.subcategory());

                    if (!subcategory.contains(feature)) {
                        valueMap.put(feature, value);
                        subcategory.add(feature);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private void processProperty(Property feature, Object value) {
        // Create category if not exist already
        if (!categories.containsKey(feature.category())) {
            categories.put(feature.category(), new TreeMap<>());
        }
        SortedMap<String, List<Property>> category = categories.get(feature.category());

        // Create subcategory if not exist already
        if (!category.containsKey(feature.subcategory())) {
            category.put(feature.subcategory(), new ArrayList<>());
        }
        List<Property> subcategory = category.get(feature.subcategory());

        // You can add field or method information to your data structures without invoking methods
        valueMap.put(feature, value);
        subcategory.add(feature);
    }
    public static class ExpandableComponent {
        public Property parent;
        Boolean enabled = false;
        HashMap<String,UIComponent> children = new HashMap<>();

        public ExpandableComponent() {}
    }
    HashMap<String,ExpandableComponent> parentElements = new HashMap<>();

    // Returns true to hide the feature if, its parent is hidden or name, description, subcategory dont contain the search query
    public boolean shouldHideFeature(Property feature) {
        boolean ignoreFeature = true;

        String parentName = feature.parentName();

        // Check if the parent element exists
        if (parentElements.containsKey(parentName)) {
            Property parent = parentElements.get(parentName).parent;

            // Recursively check parent elements
            if (parent != null) {
                ignoreFeature = shouldHideFeature(parent);
            }
        }

        // Check feature properties for search query
        if (containsIgnoreCase(feature.name(), searchQuery) ||
                containsIgnoreCase(feature.description(), searchQuery) ||
                containsIgnoreCase(feature.subcategory(), searchQuery)) {
            ignoreFeature = false;
        }

        // Check if the feature is explicitly marked as hidden
        if (feature.hidden()) {
            ignoreFeature = true;
        }

        return ignoreFeature;
    }

    private boolean containsIgnoreCase(String source, String target) {
        return source.toLowerCase().contains(target.toLowerCase());
    }

    // Reloads the current visible features by clearing old and redoing all of them
    public void reloadFeatures(UIComponent loadedFeaturesList, float guiHeight, float guiWidth, double fontScale) {
        expandedFeatures.clear();
        float Margin = 6f;
        // Default category
        // Loop through main categories
        for(String categoryName:categories.keySet()) {
            if(categoryName.contains("Developer") && !Utils.isDeveloper()) continue;

            // Makes sure that if your not searching, it wont show all the features in every page
            if(searchQuery.isEmpty()) {
                if(!categoryName.equals(selectedCategory)) {
                    continue;
                }
            }
            // Load each categorie's subcategories
            for(String subcategoryName:categories.get(categoryName).keySet()) {
                List<Property> subcategory = categories.get(categoryName).get(subcategoryName);

                int featuresVisible = 0;

                // Checks if certain features should be hidden or not
                for(Property feature:subcategory) {
                    boolean ignoreFeature = shouldHideFeature(feature);
                    if(ignoreFeature) continue;
                    featuresVisible++;
                }
                // Dont show subcategory names if no elements of it are visible
                if(featuresVisible==0) continue;
                // Render subcategory name

                UIComponent container = new UIBlock(clear).setChildOf(loadedFeaturesList)
                    .setX(new CenterConstraint())
                    .setHeight(new ChildBasedSizeConstraint())
                    .setWidth(new RelativeConstraint(1f))
                    .setY(new SiblingConstraint(Margin));

                // new Divider(subcategoryName,null).setChildOf(container);
                new UIText(subcategoryName).setChildOf(container)
                    .setY(new CenterConstraint())
                    .setX(new CenterConstraint())
                    .setTextScale(new PixelConstraint((float) fontScale*3));

                // Loop through each config option inside of Config.java
                for(Property feature:subcategory) {
                    // Cheks if should draw feature
                    if(shouldHideFeature(feature)) continue;

                    // Select texture for background of feature, outline or outline furf sky
                    String outlinePath = furfSkyThemed?"/assets/skyblockfeatures/gui/outlineFurf.png":"/assets/skyblockfeatures/gui/outline.png";

                    // Create the 'border' which is just an image that is the parent
                    UIComponent border = UIImage.ofResourceCached(outlinePath).setChildOf(loadedFeaturesList)
                        .setX(new CenterConstraint())
                        .setWidth(new RelativeConstraint(0.92f))
                        .setY(new SiblingConstraint(Margin));

                    // Example feature is the actual containing component that has the toggle switch, title, description
                    UIComponent exampleFeature = new UIBlock().setChildOf(border).setColor(clear)
                        .setX(new CenterConstraint())
                        .setY(new PixelConstraint(0f))
                        .setWidth(new PixelConstraint(0.90f*0.75f*guiWidth))
                        .setHeight(new ChildBasedSizeConstraint());

                    // Set a special exception for sliders because they modify the height more than they should
                    if(feature.type() == PropertyType.SLIDER) {
                        border.setHeight(new RelativeConstraint(0.15f));
                        exampleFeature.setHeight(new RelativeConstraint(1f));
                    }

                    // Render Color Settings different as it needs to expand
                    UIComponent colorPreview = null;
                    if(feature.type() == PropertyType.COLOR) {
                        // Color Title
                        new UIText(feature.name()).setChildOf(exampleFeature)
                            .setY(new CenterConstraint())
                            .setX(new PixelConstraint(4f))
                            .setTextScale(new PixelConstraint((float) fontScale*2f));
                        colorPreview = new UIBlock((Color) valueMap.get(feature))
                            .setChildOf(exampleFeature)
                            .setY(new CenterConstraint())
                            .setX(new PixelConstraint(120f,true))
                            .setWidth(new PixelConstraint(0.08f*0.75f*guiWidth))
                            .setHeight(new PixelConstraint(0.08f*0.75f*guiHeight))
                            .enableEffect(new OutlineEffect(Color.yellow, 1f));

                        border.setHeight(new RelativeConstraint(0.16f));
                        exampleFeature.setHeight(new RelativeConstraint(1f));
                    } else {
                        // Draw Feature Title
                        new UIText(feature.name()).setChildOf(exampleFeature)
                            .setY(new PixelConstraint(4f))
                            .setX(new PixelConstraint(4f))
                            .setTextScale(new PixelConstraint((float) fontScale*2f));
                    }

                    // Draw Feature description
                    if(feature.type() == PropertyType.TEXT) {
                        // If its a paragraph only use ~50% for the description for as to not overlap
                        new UIWrappedText(feature.description()).setChildOf(exampleFeature)
                            .setX(new PixelConstraint(4f))
                            .setWidth(new RelativeConstraint(0.5f))
                            .setColor(featureDescription)
                            .setY(new PixelConstraint(23f*(float) fontScale))
                            .setTextScale(new PixelConstraint((float) fontScale));
                    } else {
                        // Draw a normal description
                        UIComponent text =  new UIWrappedText(feature.description()).setChildOf(exampleFeature)
                            .setX(new PixelConstraint(4f))
                            .setWidth(new RelativeConstraint(0.75f))
                            .setColor(featureDescription)
                            .setY(new PixelConstraint(23f*(float) fontScale))
                            .setTextScale(new PixelConstraint((float) fontScale));
                        text.setHeight(new PixelConstraint(text.getHeight()+6));
                    }

                    if(feature.type() == PropertyType.BUTTON) {
                        UIComponent button = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/default_button.png", true), false)
                                .setHeight(new PixelConstraint(24f))
                                .setWidth(new PixelConstraint(88.5f))
                                .setY(new CenterConstraint())
                                .setX(new PixelConstraint(15f,true))
                                .setChildOf(exampleFeature);

                        UIComponent text = new UIText(feature.placeholder()).setChildOf(button).setX(new CenterConstraint()).setY(new CenterConstraint());

                        button.onMouseClickConsumer((event)->{
                            ((Runnable) valueMap.get(feature)).run();
                        });
                    }

                    // Handler for toggle features
                    if(feature.type() == PropertyType.TOGGLE) {
                        // Add the switch component
                        UIComponent comp = new SwitchComponent((Boolean) valueMap.get(feature))
                                .setChildOf(exampleFeature);
                        // Search tags are used because custom properties cannot be added onto one feature easily,
                        // So im using the first search tag to set it either as a 'parent' or the name of a feature that has 'parent'
                        if (feature.isParent()) {
                            // Create settings gear
                            ConstantColorConstraint unhovered = new ConstantColorConstraint(new Color(200,200,200));
                            ConstantColorConstraint hovered = new ConstantColorConstraint(new Color(255,255,255));

                            UIComponent settingsGear = UIImage.ofResourceCached("/assets/skyblockfeatures/gui/gear.png")
                                    .setX(new PixelConstraint(50f,true))
                                    .setY(new CenterConstraint())
                                    .setHeight(new PixelConstraint(16f))
                                    .setColor(unhovered)
                                    .setWidth(new PixelConstraint(16f))
                                    .setChildOf(exampleFeature);

                            settingsGear.onMouseEnterRunnable(()->{
                                AnimatingConstraints anim = settingsGear.makeAnimation();
                                anim.setColorAnimation(Animations.OUT_EXP,0.5f,hovered);
                                settingsGear.animateTo(anim);
                            });
                            settingsGear.onMouseLeaveRunnable(()-> {
                                AnimatingConstraints anim = settingsGear.makeAnimation();
                                anim.setColorAnimation(Animations.OUT_EXP,0.5f,unhovered);
                                settingsGear.animateTo(anim);
                            });

                            // When the gear is clicked -> go through its set children from 'parentElements' and hide/unhide them
                            settingsGear.onMouseClickConsumer((event)->{
                                Boolean val = !expandedFeatures.getOrDefault(feature.name(),false);

                                expandedFeatures.put(feature.name(),val);
                                ExpandableComponent parent = parentElements.get(feature.name());

                                for(UIComponent child : parent.children.values()) {
                                    if(val) {
                                        child.setWidth(new RelativeConstraint(1f));
                                        UIComponent child1 = child.getChildren().get(0);
                                        child.unhide(true);
                                        child.setHeight(new PixelConstraint(child1.getHeight()));
                                    } else {
                                        child.setWidth(new PixelConstraint(0f));
                                        child.hide();
                                        child.setHeight(new PixelConstraint(0f));
                                    }
                                }
                            });
                            // Set height -16 because the gear icon messes up the heigh when using ChildBasedConstraint
                            exampleFeature.setHeight(new PixelConstraint(exampleFeature.getHeight()-16f));
                        }
                        // Set the variable when the toggle switch is used
                        comp.onMouseClickConsumer((event)->{
                            Boolean val = (Boolean) getVariable(feature.name());
                            setVariable(feature.name(),!val);
                        });
                    }

                    if(feature.type() == PropertyType.COLOR) {
                        UIComponent resetImg = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/reset.png", true), false)
                                .setChildOf(exampleFeature)
                                .setY(new CenterConstraint())
                                .setWidth(new PixelConstraint(10f))
                                .setHeight(new PixelConstraint(11f))
                                .setX(new PixelConstraint(170f,true));
                        UIComponent comp = new ColorComponent((Color) valueMap.get(feature),false).setChildOf(exampleFeature);
                        final UIComponent finalColorPreview = colorPreview;

                        resetImg.onMouseClickConsumer((event)->{
                            Color defaultValue = (Color) ConfigManager.defaultValues.get(feature.name());
                            setVariable(feature.name(), defaultValue);
                            finalColorPreview.setColor(defaultValue);
                        });

                        comp.onMouseClick((event,a)->{
                            boolean featureOption = !feature.parentName().isEmpty();

                            AnimatingConstraints anim = border.makeAnimation();
                            if(featureOption) {
                                if(feature.type() == PropertyType.COLOR) {
                                    border.parent.setHeight(new PixelConstraint(80));
                                    anim.setHeightAnimation(Animations.OUT_EXP, 0.5f, new PixelConstraint(border.parent.getHeight()));
                                }
                            } else {
                                anim.setHeightAnimation(Animations.OUT_EXP, 0.5f, new RelativeConstraint(0.29f));
                            }
                            border.animateTo(anim);
                            return Unit.INSTANCE;
                        });

                        ((ColorComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
                            finalColorPreview.setColor(((Color)value));
                            return Unit.INSTANCE;
                        });
                    }

                    if(feature.type() == PropertyType.CHECKBOX) {
                        UIComponent comp = new CheckboxComponent((Boolean) valueMap.get(feature)).setChildOf(exampleFeature);
                        comp.onMouseClickConsumer((event)->{
                            Boolean val = (Boolean) getVariable(feature.name());
                            setVariable(feature.name(),!val);
                        });
                    }

                    if(feature.type() == PropertyType.DROPDOWN) {
                        UIComponent comp = new SelectorComponent((int) valueMap.get(feature),getOptions(feature.name())).setChildOf(exampleFeature);
                        ((SelectorComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
                            return Unit.INSTANCE;
                        });
                    }

                    if(feature.type() == PropertyType.TEXT) {
                        UIComponent comp = new TextComponent((String) valueMap.get(feature), "", true, false).setChildOf(exampleFeature);
                        ((TextComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
                            return Unit.INSTANCE;
                        });
                    }

                    // Custom number input, numbers only, red if nothing input
                    if(feature.type() == PropertyType.NUMBER) {
                        UIComponent comp = new UIBlock(new Color(0x232323))
                            .enableEffect(new OutlineEffect(VigilancePalette.INSTANCE.getComponentBorder(),1f))
                            .setX(new PixelConstraint(22f,true))
                            .setY(new CenterConstraint())
                            .setWidth(new PixelConstraint(58f))
                            .setHeight(new PixelConstraint(16f))
                            .setChildOf(exampleFeature);

                        UITextInput newcomp = (UITextInput) new UITextInput("")
                            .setChildOf(comp)
                            .setColor(new Color(0xBBBBBB))
                            .setWidth(new PixelConstraint(54f))
                            .setHeight(new PixelConstraint(16f))
                            .setX(new PixelConstraint(3f))
                            .setY(new PixelConstraint(4f));

                        newcomp.onMouseClickConsumer((event)->{
                            newcomp.grabWindowFocus();
                        });
                        newcomp.setText(valueMap.get(feature)+"");

                        newcomp.onKeyType((component, character, integer) -> {

                            String cleanNumber = newcomp.getText().replaceAll("[^0-9]", "");
                            if(cleanNumber.isEmpty()) {
                                comp.setColor(new Color(0x401613));
                                newcomp.setText("0");
                            } else {
                                comp.setColor(new Color(0x232323));
                            }
                            int value = 0;
                            try {
                                value = Integer.parseInt(cleanNumber);
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                            setVariable(feature.name(),value);
                            newcomp.setText(cleanNumber);
                            return Unit.INSTANCE;
                        });
                    }

                    if(feature.type() == PropertyType.SLIDER) {
                        UIComponent comp = new SliderComponent((Integer) valueMap.get(feature), feature.min(), feature.max()).setChildOf(exampleFeature);
                        ((SliderComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
                            return Unit.INSTANCE;
                        });
                    }

                    border.setHeight(new PixelConstraint(exampleFeature.getHeight()));

                    String parentName = feature.parentName();

                    if(feature.isParent()) {
                        Boolean enabled = (Boolean) getVariable(feature.name());
                        ExpandableComponent comp = new ExpandableComponent();
                        comp.enabled = enabled;
                        comp.parent = feature;
                        parentElements.put(feature.name(),comp);
                    } else if(!feature.parentName().isEmpty()) {
                        border.setWidth(new RelativeConstraint(.85f));
                        exampleFeature.setWidth(new RelativeConstraint(1f));

                        UIComponent test = new UIBlock(clear)
                                .setChildOf(loadedFeaturesList)
                                .setWidth(new PixelConstraint(0f))
                                .setHeight(new PixelConstraint(0f))
                                .setY(new SiblingConstraint(Margin))
                                .enableEffect(new ScissorEffect());

                        if(!searchQuery.isEmpty()) {
                            test.setWidth(new RelativeConstraint(1f));
                            test.setHeight(new PixelConstraint(border.getHeight()));
                        }
                        loadedFeaturesList.removeChild(border);
                        test.addChild(border);
                        if(parentElements.containsKey(parentName)) {
                            parentElements.get(parentName).children.put(feature.name(),test);
                        }
                    }
                }
            }
        }
    }
    public void setVariable(String name, Object newValue) {
        Config config = SkyblockFeatures.config;
        Arrays.stream(config.getClass().getFields())
            .filter(field -> field.isAnnotationPresent(Property.class) && field.getAnnotation(Property.class).name().equals(name))
            .findFirst()
            .ifPresent(field -> {
                field.setAccessible(true);
                try { field.set(config, newValue); } catch (Exception e) { e.printStackTrace(); }
            });
        reloadAllCategories();
    }

    public Object getVariable(String name) {
        Config config = SkyblockFeatures.config;
        return Arrays.stream(config.getClass().getFields())
                    .filter(field -> field.isAnnotationPresent(Property.class) && field.getAnnotation(Property.class).name().equals(name))
                    .findFirst()
                    .map(field -> {
                        field.setAccessible(true);
                        try { return field.get(config); } catch (Exception e) { e.printStackTrace(); }
                        return null;
                    })
                    .orElse(null);
    }

    public List<String> getOptions(String name) {
        return Arrays.stream(SkyblockFeatures.config.getClass().getFields())
                    .filter(field -> field.isAnnotationPresent(Property.class) && field.getAnnotation(Property.class).name().equals(name))
                    .findFirst()
                    .map(field -> Arrays.asList(field.getAnnotation(Property.class).options()))
                    .orElse(null);
    }


    public void LoadCategory(String categoryName) {
        quickSwapping = true;
        selectedCategory = categoryName;
        GuiUtils.openGui(new ConfigGui(false));
    }
}