package mrfast.skyblockfeatures.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import gg.essential.api.utils.GuiUtil;
import gg.essential.elementa.ElementaVersion;
import gg.essential.elementa.UIComponent;
import gg.essential.elementa.WindowScreen;
import gg.essential.elementa.components.ScrollComponent;
import gg.essential.elementa.components.UIBlock;
import gg.essential.elementa.components.UIRoundedRectangle;
import gg.essential.elementa.components.UIText;
import gg.essential.elementa.components.UIWrappedText;
import gg.essential.elementa.components.inspector.Inspector;
import gg.essential.elementa.constraints.CenterConstraint;
import gg.essential.elementa.constraints.ChildBasedSizeConstraint;
import gg.essential.elementa.constraints.PixelConstraint;
import gg.essential.elementa.constraints.RelativeConstraint;
import gg.essential.elementa.constraints.animation.AnimatingConstraints;
import gg.essential.elementa.constraints.animation.Animations;
import gg.essential.elementa.effects.OutlineEffect;
import gg.essential.elementa.effects.RecursiveFadeEffect;
import gg.essential.elementa.effects.ScissorEffect;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyType;
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
import mrfast.skyblockfeatures.SkyblockFeatures;
import mrfast.skyblockfeatures.core.Config;
import mrfast.skyblockfeatures.utils.Utils;

public class TestGui extends WindowScreen {
    public static SortedMap<String, SortedMap<String,List<Property>>> catagories = new TreeMap<>();
    public static HashMap<Property, Object> valueMap = new HashMap<>();
    public static String selectedCatagory = "General";
    public String searchQuery = "";
    @Override
	public void onScreenClose() {
		SkyblockFeatures.config.markDirty();
        SkyblockFeatures.config.writeData();
	}

    // Text/Lines colors
    Color titleColor = SkyblockFeatures.config.titleColor;//new Color(0x00FFFF);
    Color guiLines = SkyblockFeatures.config.guiLines;
    Color selectedCategory = SkyblockFeatures.config.selectedCategory;
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
    public TestGui(Boolean doAnimation) {
        super(ElementaVersion.V2);
        reloadAllCatagories();
        
        int screenHeight = Utils.GetMC().currentScreen.height;
        UIComponent box = new UIRoundedRectangle(10f)
            .setX(new CenterConstraint())
            .setY(new CenterConstraint())
            .setWidth(new RelativeConstraint(0.70f))
            .setHeight(new RelativeConstraint(0.70f))
            .setChildOf(getWindow())
            .setColor(mainBackground)
            .enableEffect(new ScissorEffect());
        new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/largeOutline.png",false),false).setChildOf(box)
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

        new Inspector(getWindow()).setChildOf(getWindow());
        
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

        // Area of where the currently selected catagorie's feature will be displayed
        UIComponent loadedFeaturesList = new ScrollComponent("No Matching Settings Found", 10f, featureBoxOutline, false, true, false, false, 25f, 1f, null)
            .setX(new PixelConstraint(0.25f*guiWidth))
            .setY(new PixelConstraint(titleArea.getHeight()))
            // .setColor(Color.red)
            .enableEffect(new ScissorEffect())
            .setWidth(new PixelConstraint(0.75f*guiWidth))
            .setHeight(new PixelConstraint(((0.85f*guiHeight)-1)));
        loadedFeaturesList.clearChildren();
        reloadFeatures(loadedFeaturesList,guiHeight,guiWidth,fontScale);

        input.onKeyType((component, character, integer) -> {
            searchQuery = ((UITextInput) component).getText().toLowerCase();
            loadedFeaturesList.clearChildren();
            reloadFeatures(loadedFeaturesList,guiHeight,guiWidth,fontScale);
            return Unit.INSTANCE;
        });
        
        // Side bar on the left that holds the catagories
        UIComponent sidebarArea = new UIBlock()
            .setX(new PixelConstraint(0f))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setWidth(new PixelConstraint(0.25f*guiWidth))
            .setHeight(new PixelConstraint(0.85f*guiHeight))
            .setChildOf(getWindow())
            .setColor(clear)
            .enableEffect(new ScissorEffect());
        // Seperator to the right side of the sidebar
        UIComponent sidebarSeperator = new UIBlock()
            .setWidth(new PixelConstraint(1f))
            .setHeight(new PixelConstraint((0.85f*guiHeight)-1))
            .setX(new PixelConstraint(0.25f*guiWidth))
            .setY(new PixelConstraint(titleArea.getHeight()))
            .setColor(guiLines);
        int Index = 0;

        // Draw catagorys on sidebar
        for(String catagoryName:catagories.keySet()) {
            UIComponent ExampleCatagory = new UIText(catagoryName)
                .setChildOf(sidebarArea)
                .setColor(defaultCategory)
                .setX(new CenterConstraint())
                .setY(new PixelConstraint(10f+(Index*20)))
                .enableEffect(new RecursiveFadeEffect())
                .setTextScale(new PixelConstraint((float) fontScale*2));
                
            // Set color of selected category
            if(catagoryName.equals(selectedCatagory)) {
                ExampleCatagory.setColor(selectedCategory);
            }
            ExampleCatagory.onMouseEnterRunnable(()->{
                if(!catagoryName.equals(selectedCatagory)) {
                    ExampleCatagory.setColor(hoveredCategory);
                }
            });
            ExampleCatagory.onMouseLeaveRunnable(()->{
                if(!catagoryName.equals(selectedCatagory)) ExampleCatagory.setColor(defaultCategory);
            });
            ExampleCatagory.onMouseClickConsumer((event)->{
                selectedCatagory = catagoryName;
                LoadCatagory(catagoryName);
            });
            Index++;
        }

        UIComponent editGuiButton = new UIRoundedRectangle(10f).setColor(editGuiUnhovered)
            .setX(new PixelConstraint(0.15f*0.25f*guiWidth))
            .setY(new PixelConstraint(0.90f*guiHeight))
            .setHeight(new PixelConstraint(0.85f*0.10f*guiHeight))
            .setWidth(new PixelConstraint(0.70f*0.25f*guiWidth))
            .setChildOf(sidebarArea);
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
            GuiUtil.open(new EditLocationsGui());
        });
        UIComponent discordButton = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/discord.png",true),true)
            .setX(new RelativeConstraint(0.025f))
            .setWidth(new PixelConstraint(30))
            .setHeight(new PixelConstraint(30))
            .setChildOf(titleArea)
            .setY(new CenterConstraint());
        discordButton.onMouseClickConsumer((event)->{
            try {
                Desktop.getDesktop().browse(new URI("https://discord.gg/MDTEAjbNw8"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
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
        if(selectedCatagory.contains("Customization")) {
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
                GuiUtil.open(new TestGui(false));
            });
        }
    }

    public void reloadAllCatagories() {
        catagories.clear();
        Config field = SkyblockFeatures.config;
        Field[] fieldsOfFieldClass = Config.class.getFields();
        for(int i = 0;i < fieldsOfFieldClass.length; i++) {
            try {
                Object value = fieldsOfFieldClass[i].get(field);
                if (fieldsOfFieldClass[i].isAnnotationPresent(Property.class)) {
                    Property feature = fieldsOfFieldClass[i].getAnnotation(Property.class);
                    // Create catagory if not exist already
                    if(!catagories.containsKey(feature.category())) {
                        catagories.put(feature.category(), new TreeMap<>());
                    }
                    SortedMap<String, List<Property>> catagory = catagories.get(feature.category());

                    // Create subcatagory if not exist already
                    if(!catagory.containsKey(feature.subcategory())) {
                        catagory.put(feature.subcategory(), new ArrayList<>());
                    }
                    List<Property> subcatagory = catagory.get(feature.subcategory());
            
                    if(!subcatagory.contains(feature)) {
                        valueMap.put(feature, value);
                        subcatagory.add(feature);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public void reloadFeatures(UIComponent loadedFeaturesList, float guiHeight, float guiWidth, double fontScale) {
        int index = 0; 
        int yOffset = 0;
        float Margin = 6f;
        // Default catagory
        for(String catagoryName:catagories.keySet()) {
            if(searchQuery.isEmpty()) {
                if(!catagoryName.equals(selectedCatagory)) {
                    continue;
                }
            }

            for(String subcatagoryName:catagories.get(catagoryName).keySet()) {
                List<Property> subcatagory = catagories.get(catagoryName).get(subcatagoryName);
                int featuresVisible = 0;
                for(Property feature:subcatagory) {
                    if((!feature.name().toLowerCase().contains(searchQuery) && !feature.description().toLowerCase().contains(searchQuery)) || feature.hidden()) {
                        continue;
                    }
                    featuresVisible++;
                }
                // Dont show subcatagory names if no elements of it are visible
                if(featuresVisible==0) continue;
                // Render subcatagory name

                UIComponent container = new UIBlock(clear).setChildOf(loadedFeaturesList)
                    .setX(new CenterConstraint())
                    .setHeight(new ChildBasedSizeConstraint())
                    .setWidth(new RelativeConstraint(1f))
                    .setY(new PixelConstraint(yOffset+Margin));

                // new Divider(subcatagoryName,null).setChildOf(container);
                new UIText(subcatagoryName).setChildOf(container)
                    .setY(new CenterConstraint())
                    .setX(new CenterConstraint())
                    .setTextScale(new PixelConstraint((float) fontScale*3));

                index++;
                yOffset += container.getHeight() + Margin;
                for(Property feature:subcatagory) {
                    if((!feature.name().toLowerCase().contains(searchQuery) && !feature.description().toLowerCase().contains(searchQuery)) || feature.hidden()) {
                        continue;
                    }
                    UIComponent border = new ShadowIcon(new ResourceImageFactory("/assets/skyblockfeatures/gui/outline.png",false),false).setChildOf(loadedFeaturesList)
                        .setX(new CenterConstraint())
                        .setWidth(new RelativeConstraint(0.92f))
                        .setY(new PixelConstraint(yOffset+Margin));
                    
                    UIComponent exampleFeature = new UIBlock().setChildOf(loadedFeaturesList).setColor(clear)
                        .setX(new CenterConstraint())
                        .setY(new PixelConstraint(yOffset+Margin))
                        .setWidth(new PixelConstraint(0.90f*0.75f*guiWidth))
                        .setHeight(new ChildBasedSizeConstraint());
                        
                    if(feature.type() == PropertyType.SLIDER) {
                        exampleFeature.setHeight(new RelativeConstraint(0.15f));
                    }
                    if(feature.type() == PropertyType.COLOR) {
                        // Color Title
                        new UIText(feature.name()).setChildOf(exampleFeature)
                            .setY(new CenterConstraint())
                            .setX(new PixelConstraint(4f))
                            .setTextScale(new PixelConstraint((float) fontScale*2f));
                        new UIBlock((Color) valueMap.get(feature))
                            .setChildOf(exampleFeature)
                            .setY(new CenterConstraint())
                            .setX(new RelativeConstraint(0.66f))
                            .setWidth(new PixelConstraint(0.08f*0.75f*guiWidth))
                            .setHeight(new PixelConstraint(0.08f*0.75f*guiHeight))
                            .enableEffect(new OutlineEffect(Color.yellow, 1f));
                            
                        exampleFeature.setHeight(new RelativeConstraint(0.29f));
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
                        comp.onMouseClickConsumer((event)->{
                            Boolean val = (Boolean) getVariable(feature.name());
                            setVariable(feature.name(),!val);
                        });
                    }

                    if(feature.type() == PropertyType.COLOR) {
                        UIComponent comp = new ColorComponent((Color) valueMap.get(feature),false).setChildOf(exampleFeature);
                        ((ColorComponent) comp).onValueChange((value)->{
                            setVariable(feature.name(),value);
                            GuiUtil.open(new TestGui(false));
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

                    index++;
                    yOffset += exampleFeature.getHeight() + Margin;
                }
            }
        }
    }

    public void setVariable(String name,Object newValue) {
        Config field = SkyblockFeatures.config;
        Field[] fieldsOfFieldClass = Config.class.getFields();
        for(int i = 0;i < fieldsOfFieldClass.length; i++) {
            try {
                if (fieldsOfFieldClass[i].isAnnotationPresent(Property.class)) {
                    Property featureProperty = fieldsOfFieldClass[i].getAnnotation(Property.class);
                    if(featureProperty.name()==name) {
                        fieldsOfFieldClass[i].setAccessible(true);
                        fieldsOfFieldClass[i].set(field, newValue);
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        reloadAllCatagories();
    }

    public Object getVariable(String name) {
        Config field = SkyblockFeatures.config;
        Field[] fieldsOfFieldClass = Config.class.getFields();
        for(int i = 0;i < fieldsOfFieldClass.length; i++) {
            try {
                Object value = fieldsOfFieldClass[i].get(field);
                if (fieldsOfFieldClass[i].isAnnotationPresent(Property.class)) {
                    Property featureProperty = fieldsOfFieldClass[i].getAnnotation(Property.class);
                    if(featureProperty.name()==name) {
                        return value;
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return null;
    }

    public ArrayList getOptions(String name) {
        Field[] fieldsOfFieldClass = Config.class.getFields();
        for(int i = 0;i < fieldsOfFieldClass.length; i++) {
            try {
                if (fieldsOfFieldClass[i].isAnnotationPresent(Property.class)) {
                    Property featureProperty = fieldsOfFieldClass[i].getAnnotation(Property.class);
                    if(featureProperty.name()==name) {
                        return new ArrayList(Arrays.asList(featureProperty.options()));
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return null;
    }

    public void LoadCatagory(String catagoryName) {
        GuiUtil.open(new TestGui(false));
    }
}