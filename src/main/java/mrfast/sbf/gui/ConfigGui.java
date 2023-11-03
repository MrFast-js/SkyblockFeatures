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

import com.mojang.realmsclient.gui.ChatFormatting;
import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.ScrollComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIImage;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.UIWrappedText;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.ChildBasedSizeConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.SiblingConstraint;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.effects.OutlineEffect;
import gg.essential.elementa.effects.RecursiveFadeEffect;
import gg.essential.elementa.effects.ScissorEffect;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
import gg.essential.vigilance.gui.VigilancePalette;
import gg.essential.vigilance.gui.common.input.UITextInput;
import gg.essential.vigilance.gui.common.shadow.ShadowIcon;
import gg.essential.vigilance.gui.settings.CheckboxComponent;
import gg.essential.vigilance.gui.settings.ColorComponent;
import gg.essential.vigilance.gui.settings.SelectorComponent;
import gg.essential.vigilance.gui.settings.SliderComponent;
import gg.essential.vigilance.gui.settings.SwitchComponent;
import gg.essential.vigilance.gui.settings.TextComponent;
import gg.essential.vigilance.utils.ResourceImageFactory;
import kotlin.Unit;
import mrfast.sbf.SkyblockFeatures;
import mrfast.sbf.core.Config;
import mrfast.sbf.utils.Utils;

public class ConfigGui extends WindowScreen {
    public static SortedMap<String, SortedMap<String,List<Property>>> categories = new TreeMap<>();
    public static HashMap<Property, Object> valueMap = new HashMap<>();
    public static String selectedCategory = "General";
    public String searchQuery = "";
    static Boolean furfSkyThemed = false;

    @Override
	public void onScreenClose() {
		SkyblockFeatures.config.forceSave();
	}

    // Text/Lines colors
    Color titleColor = SkyblockFeatures.config.titleColor;//new Color(0x00FFFF);
    Color guiLines = SkyblockFeatures.config.guiLines;
    Color selectedCategoryColor = SkyblockFeatures.config.selectedCategory;
    Color hoveredCategory = SkyblockFeatures.config.hoveredCategory;
    Color defaultCategory = SkyblockFeatures.config.defaultCategory;
    Color versionText = SkyblockFeatures.config.versionColor;//new Color(0xFFFFFF);
    Color editGuiText = SkyblockFeatures.config.editGuiText;
    Color featureBoxOutline = SkyblockFeatures.config.featureBoxOutline;
    Color featureDescription = SkyblockFeatures.config.featureDescription;

    // Background colors
    Color mainBackground = SkyblockFeatures.config.mainBackground;
    Color searchBoxBackground = SkyblockFeatures.config.searchBoxBackground;

    Color editGuiUnhovered = SkyblockFeatures.config.editGuiUnhovered;
    Color editGuiHovered = SkyblockFeatures.config.editGuiHovered;
    
    Color clear = new Color(0,0,0,0);
    public ConfigGui(Boolean doAnimation) {
        super(ElementaVersion.V2);
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

        if(Utils.isDeveloper()) {
            // new Inspector(getWindow()).setChildOf(getWindow());
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
            input.grabWindowFocus();;
        });
        
        // Gray horizontal line 1px from bottom of the title area
        new UIBlock().setChildOf(titleArea)
            .setWidth(new PixelConstraint(guiWidth-2))
            .setHeight(new PixelConstraint(1f))
            .setX(new CenterConstraint())
            .setY(new PixelConstraint(titleArea.getHeight()-1))
            .setColor(guiLines);

        // Area of where the currently selected categorie's feature will be displayed
        UIComponent loadedFeaturesList = new ScrollComponent("No Matching Settings Found", 10f, featureBoxOutline, false, true, false, false, 25f, 1f, null)
            .setX(new PixelConstraint(0.25f*guiWidth))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .enableEffect(new ScissorEffect())
            .setWidth(new PixelConstraint(0.75f*guiWidth))
            .setHeight(new PixelConstraint(((0.85f*guiHeight)-(furfSkyThemed?6:1))));
        loadedFeaturesList.clearChildren();
        reloadFeatures(loadedFeaturesList,guiHeight,guiWidth,fontScale);

        input.onKeyType((component, character, integer) -> {
            searchQuery = ((UITextInput) component).getText().toLowerCase();
            loadedFeaturesList.clearChildren();
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

        UIComponent sidebarAreaScroll = new ScrollComponent("No Matching Settings Found", 10f, featureBoxOutline, false, true, false, false, 25f, 1f, null)
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
                    ExampleCategory.setColor(hoveredCategory);
                }
            });
            ExampleCategory.onMouseLeaveRunnable(()->{
                if(!categoryName.equals(selectedCategory)) ExampleCategory.setColor(defaultCategory);
            });
            ExampleCategory.onMouseClickConsumer((event)->{
                if(selectedCategory==categoryName) return;
                selectedCategory = categoryName;
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
            editGuiButton = new UIRoundedRectangle(10f)
                    .setColor(editGuiUnhovered)
                    .setX(new PixelConstraint(0.15f * 0.25f * guiWidth))
                    .setY(new PixelConstraint(0.90f * guiHeight))
                    .setHeight(new PixelConstraint(0.85f * 0.10f * guiHeight))
                    .setWidth(new PixelConstraint(0.70f * 0.25f * guiWidth))
                    .setChildOf(sidebarArea);
        }
        
        new UIText("Edit Gui Locations").setColor(editGuiText).setChildOf(editGuiButton)
            .setTextScale(new PixelConstraint((float) fontScale))
            .setX(new CenterConstraint())
            .setY(new CenterConstraint());

        editGuiButton.onMouseEnterRunnable(()->{
            editGuiButton.setColor(editGuiHovered);
        });
        editGuiButton.onMouseLeaveRunnable(()->{
            editGuiButton.setColor(editGuiUnhovered);
        });
        // Open gui locations gui when clicked
        editGuiButton.onMouseClickConsumer((event)->{
            if(isShiftKeyDown()) {
                Utils.overrideDevMode = true;
                Utils.overrideDevModeValue = !Utils.overrideDevModeValue;
                if(Utils.overrideDevModeValue) {
                    Utils.SendMessage(ChatFormatting.YELLOW+"Developer Mode Enabled!");
                } else {
                    Utils.SendMessage(ChatFormatting.YELLOW+"Developer Mode Disabled!");
                }
                Utils.playSound("random.orb", 0.1);
                return;
            }
            Utils.openGui(new EditLocationsGui());
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
        
        if(doAnimation) {
            box.setWidth(new PixelConstraint(0f));

            AnimatingConstraints anim = box.makeAnimation();
            anim.setWidthAnimation(Animations.OUT_EXP, 0.5f, new RelativeConstraint(0.70f));
            box.animateTo(anim);

            AnimatingConstraints animation = titleText.makeAnimation();
            animation.setTextScaleAnimation(Animations.OUT_EXP, 0.5f, new PixelConstraint((float) (4.0*fontScale)));
            titleText.animateTo(animation);
        }
        if(selectedCategory.contains("Customization")) {
            UIComponent resetGuiColorsButton = new UIRoundedRectangle(10f).setColor(editGuiUnhovered)
                .setX(new RelativeConstraint(0.65f))
                .setY(new PixelConstraint(0.01f*guiHeight))
                .setHeight(new PixelConstraint(0.85f*0.10f*guiHeight))
                .setWidth(new PixelConstraint(0.23f*guiWidth))
                .setChildOf(loadedFeaturesList);
            new UIText("§cReset Colors").setColor(editGuiText).setChildOf(resetGuiColorsButton)
                .setTextScale(new PixelConstraint((float) fontScale*2))
                .setX(new CenterConstraint())
                .setY(new CenterConstraint());

            resetGuiColorsButton.onMouseEnterRunnable(()->{
                resetGuiColorsButton.setColor(editGuiHovered);
            });
            resetGuiColorsButton.onMouseLeaveRunnable(()->{
                resetGuiColorsButton.setColor(editGuiUnhovered);
            });
            // Open gui locations gui when clicked
            resetGuiColorsButton.onMouseClickConsumer((event)->{
                SkyblockFeatures.config.guiLines = new Color(0x808080);
                SkyblockFeatures.config.selectedCategory = new Color(0x02A9EA);
                SkyblockFeatures.config.hoveredCategory = new Color(0x2CC8F7);
                SkyblockFeatures.config.defaultCategory = new Color(0xFFFFFF);
                SkyblockFeatures.config.featureBoxOutline = new Color(0xa9a9a9);
                SkyblockFeatures.config.featureDescription = new Color(0xbbbbbb);
                SkyblockFeatures.config.mainBackground = new Color(25,25,25,200);
                SkyblockFeatures.config.searchBoxBackground = new Color(120,120,120,60);
                SkyblockFeatures.config.editGuiUnhovered = new Color(0,0,0,50);
                SkyblockFeatures.config.editGuiHovered = new Color(0,0,0,75);
                SkyblockFeatures.config.editGuiText = new Color(0xFFFFFF);
                SkyblockFeatures.config.titleColor = new Color(0x00FFFF);
                SkyblockFeatures.config.versionColor = new Color(0xFFFFFF);
                Utils.openGui(new ConfigGui(false));
            });
        }
    }

    public void reloadAllCategories() {
        categories.clear();
        Config field = SkyblockFeatures.config;
        Field[] fieldsOfFieldClass = Config.class.getFields();
        for(int i = 0;i < fieldsOfFieldClass.length; i++) {
            try {
                Object value = fieldsOfFieldClass[i].get(field);
                if (fieldsOfFieldClass[i].isAnnotationPresent(Property.class)) {
                    Property feature = fieldsOfFieldClass[i].getAnnotation(Property.class);
                    // Create category if not exist already
                    if(!categories.containsKey(feature.category())) {
                        categories.put(feature.category(), new TreeMap<>());
                    }
                    SortedMap<String, List<Property>> category = categories.get(feature.category());

                    // Create subcategory if not exist already
                    if(!category.containsKey(feature.subcategory())) {
                        category.put(feature.subcategory(), new ArrayList<>());
                    }
                    List<Property> subcategory = category.get(feature.subcategory());
            
                    if(!subcategory.contains(feature)) {
                        valueMap.put(feature, value);
                        subcategory.add(feature);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }
    public class ExpandableComponent {
        Boolean enabled = false;
        UIComponent parent = null;
        String parentName = "";
        List<UIComponent> children = new ArrayList<>();

        public ExpandableComponent() {

        }
    }
    HashMap<String,ExpandableComponent> parentStuff = new HashMap<>();
    public void reloadFeatures(UIComponent loadedFeaturesList, float guiHeight, float guiWidth, double fontScale) {
        int yOffset = 0;
        float Margin = 6f;
        // Default category
        for(String categoryName:categories.keySet()) {
            if(searchQuery.isEmpty()) {
                if(!categoryName.equals(selectedCategory)) {
                    continue;
                }
            }

            for(String subcategoryName:categories.get(categoryName).keySet()) {
                List<Property> subcategory = categories.get(categoryName).get(subcategoryName);
                int featuresVisible = 0;
                for(Property feature:subcategory) {
                    boolean ignoreFeature = true;
                    if(feature.name().toLowerCase().contains(searchQuery)) ignoreFeature = false;
                    if(feature.description().toLowerCase().contains(searchQuery)) ignoreFeature =false;
                    if(feature.subcategory().toLowerCase().contains(searchQuery)) ignoreFeature = false;
                    if(feature.hidden()) ignoreFeature = true;

                    if(feature.searchTags().length>0) {
                        String tag = feature.searchTags()[0];
                        if(parentStuff.get(tag)!=null) {
                            Boolean enabled = parentStuff.get(tag).enabled;
                            if(!enabled) {
                                ignoreFeature = true;
                            }
                        }
                    }

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

                // yOffset += container.getHeight() + Margin;
                for(Property feature:subcategory) {
                    if(feature.hidden()) continue;

                    if(!feature.name().toLowerCase().contains(searchQuery) && !feature.description().toLowerCase().contains(searchQuery)) {
                        boolean shouldShow = false;
                        if(feature.subcategory().toLowerCase().contains(searchQuery)) shouldShow = true;
                        if(feature.searchTags().length>0) {
                            String tag = feature.searchTags()[0];
                            if(parentStuff.get(tag)!=null) {
                                Boolean enabled = parentStuff.get(tag).enabled;
                                if(enabled) {
                                    shouldShow = true;
                                }
                            }
                        }
                        if(!shouldShow) continue;
                    }

                    String outlinePath = furfSkyThemed?"/assets/skyblockfeatures/gui/outlineFurf.png":"/assets/skyblockfeatures/gui/outline.png";

                    UIComponent border = UIImage.ofResourceCached(outlinePath).setChildOf(loadedFeaturesList)
                        .setX(new CenterConstraint())
                        .setWidth(new RelativeConstraint(0.92f))
                        .setY(new SiblingConstraint(Margin));

                    UIComponent exampleFeature = new UIBlock().setChildOf(border).setColor(clear)
                        .setX(new CenterConstraint())
                        .setY(new PixelConstraint(0f))
                        .setWidth(new PixelConstraint(0.90f*0.75f*guiWidth))
                        .setHeight(new ChildBasedSizeConstraint());
                    
                    if(feature.type() == PropertyType.SLIDER) {
                        border.setHeight(new RelativeConstraint(0.15f));
                        exampleFeature.setHeight(new RelativeConstraint(1f));
                    }
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
                        // Feature Title
                        new UIText(feature.name()).setChildOf(exampleFeature)
                            .setY(new PixelConstraint(4f))
                            .setX(new PixelConstraint(4f))
                            .setTextScale(new PixelConstraint((float) fontScale*2f));
                    }
        
                    // Feature description
                    if(feature.type() == PropertyType.PARAGRAPH) {
                        new UIWrappedText(feature.description()).setChildOf(exampleFeature)
                            .setX(new PixelConstraint(4f))
                            .setWidth(new RelativeConstraint(0.5f))
                            .setColor(featureDescription)
                            .setY(new PixelConstraint(23f*(float) fontScale))
                            .setTextScale(new PixelConstraint((float) fontScale*1f));
                    } else if(feature.type() == PropertyType.TEXT) {
                        new UIWrappedText(feature.description()).setChildOf(exampleFeature)
                            .setX(new PixelConstraint(4f))
                            .setWidth(new RelativeConstraint(0.75f))
                            .setColor(featureDescription)
                            .setY(new PixelConstraint(23f*(float) fontScale))
                            .setTextScale(new PixelConstraint((float) fontScale*1f));
                    } else {
                        UIComponent text =  new UIWrappedText(feature.description()).setChildOf(exampleFeature)
                            .setX(new PixelConstraint(4f))
                            .setWidth(new RelativeConstraint(0.75f))
                            .setColor(featureDescription)
                            .setY(new PixelConstraint(23f*(float) fontScale))
                            .setTextScale(new PixelConstraint((float) fontScale*1f));
                        text.setHeight(new PixelConstraint(text.getHeight()+6));
                    }
                    
        
                    if(feature.type() == PropertyType.SWITCH) {
                        UIComponent comp = new SwitchComponent((Boolean) valueMap.get(feature)).setChildOf(exampleFeature);
                        // Sub subs
                        comp.onMouseClickConsumer((event)->{
                            Boolean val = (Boolean) getVariable(feature.name());
                            setVariable(feature.name(),!val);
                            
                            if(feature.searchTags().length>0) {
                                String tag = feature.searchTags()[0];
                                if(tag.equals("parent")) {
                                    ExpandableComponent parent = parentStuff.get(feature.name());
                                    parent.enabled = !val;
                                    for(UIComponent child : parent.children) {
                                        if(parent.enabled) {
                                            child.unhide(true);
                                        } else {
                                            child.hide();
                                        }
                                    }
                                }
                            }
                        });
                    }

                    if(feature.type() == PropertyType.COLOR) {
                        UIComponent comp = new ColorComponent((Color) valueMap.get(feature),false).setChildOf(exampleFeature);
                        
                        final UIComponent finalColorPreview = colorPreview;

                        comp.onMouseClick((event,a)->{
                            AnimatingConstraints anim = border.makeAnimation();
                            anim.setHeightAnimation(Animations.OUT_EXP, 0.5f, new RelativeConstraint(0.29f));
                            border.animateTo(anim);
                            return Unit.INSTANCE;
                        });
                        // comp.onMouseLeave((event)->{
                        //     AnimatingConstraints anim = border.makeAnimation();
                        //     anim.setHeightAnimation(Animations.IN_EXP, 0.5f, new RelativeConstraint(0.16f));
                        //     border.animateTo(anim);
                        //     ((ColorComponent) comp).closePopups(false);
                        //      ((ColorComponent) comp).
                        //     return Unit.INSTANCE;
                        // });
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
        
                    if(feature.type() == PropertyType.SELECTOR) {
                        UIComponent comp = new SelectorComponent((int) valueMap.get(feature),getOptions(feature.name())).setChildOf(exampleFeature);
                        ((SelectorComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
                            return Unit.INSTANCE;
                        });
                    }

                    if(feature.type() == PropertyType.TEXT) {
                        UIComponent comp = new TextComponent((String) valueMap.get(feature), "", false, false).setChildOf(exampleFeature);
                        if(feature.name().contains("API")) {
                            comp = new TextComponent((String) valueMap.get(feature), "", false, true).setChildOf(exampleFeature);
                        }
                        ((TextComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
                            return Unit.INSTANCE;
                        });
                    }

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
                            newcomp.grabWindowFocus();;
                        });
                        newcomp.setText(valueMap.get(feature)+"");
                        
                        newcomp.onKeyType((component, character, integer) -> {
                            String cleanNumber = ((String) newcomp.getText()).replaceAll("[^0-9]", "");
                            if(cleanNumber.length()==0) {
                                comp.setColor(new Color(0x401613));
                                newcomp.setText("0");
                            } else {
                                comp.setColor(new Color(0x232323));
                            }
                            Integer value = 0;
                            try {
                                value = Integer.parseInt(cleanNumber);
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                            setVariable(feature.name(),value);
                            newcomp.setText(cleanNumber+"");
                            return Unit.INSTANCE;
                        });
                    }

                    if(feature.type() == PropertyType.PARAGRAPH) {
                        UIComponent comp = new TextComponent((String) valueMap.get(feature), "", true, false).setChildOf(exampleFeature);
                        ((TextComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
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

                    if(feature.searchTags().length>0) {
                        String tag = feature.searchTags()[0];
                        if(tag.equals("parent")) {
                            Boolean enabled = (Boolean) getVariable(feature.name());
                            ExpandableComponent comp = new ExpandableComponent();
                            comp.enabled = enabled;
                            comp.parent = border;
                            parentStuff.put(feature.name(),comp);
                        } else {
                            if(parentStuff.get(tag)!=null) {
                                Boolean enabled = parentStuff.get(tag).enabled;
                                border.setWidth(new RelativeConstraint(.85f));  
                                exampleFeature.setWidth(new RelativeConstraint(1f));
                                parentStuff.get(tag).children.add(border);

                                if(!enabled) {
                                    border.hide();
                                    // exampleFeature.hide();
                                    continue;
                                };
                            }
                        }
                    }

                    // yOffset += exampleFeature.getHeight() + Margin;
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
        Utils.openGui(new ConfigGui(false));
    }
}