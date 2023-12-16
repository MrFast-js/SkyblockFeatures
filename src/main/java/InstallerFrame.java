import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

public class InstallerFrame extends JFrame {

    private JComboBox<String> versionDropdown;
    private JComboBox<String> folderDropdown;

    public InstallerFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Skyblock Features Installer");
        setSize(600, 300);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Logo
        ImageIcon originalIcon = new ImageIcon(getClass().getClassLoader().getResource("logo.png"));
        Image originalImage = originalIcon.getImage();
        Image scaledImage = originalImage.getScaledInstance(originalIcon.getIconWidth()/12, originalIcon.getIconHeight()/12, Image.SCALE_SMOOTH); // Adjust size as needed
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel logoLabel = new JLabel(scaledIcon);
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(logoLabel);

        // Description
        JTextArea descriptionTextArea = new JTextArea(
                "This installer will copy Skyblock Features into your selected mods folder for you, and replace any old versions that already exist. Skyblock Features is a 1.8.9 Forge Hypixel Skyblock mod that adds features that are not commonly found or are paid in other mods but are very useful. SBF is free and will always be free.");
        descriptionTextArea.setWrapStyleWord(true);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setEditable(false);
        descriptionTextArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        descriptionTextArea.setBackground(panel.getBackground());


        // Center-align the text area within a panel
        JPanel centeredPanel = new JPanel(new BorderLayout());
        centeredPanel.add(descriptionTextArea, BorderLayout.CENTER);

        // Add left and right margins
        int margin = 20;
        centeredPanel.setBorder(BorderFactory.createEmptyBorder(0, margin, 0, margin));

        panel.add(centeredPanel);

        panel.add(descriptionTextArea);

        // Version Dropdown
        JPanel versionDropdownPanel = new JPanel();
        versionDropdownPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        versionDropdownPanel.add(new JLabel("Version: "));
        versionDropdown = new JComboBox<>();
        versionDropdownPanel.add(versionDropdown);
        panel.add(versionDropdownPanel);

        // Folder Dropdown
        JPanel folderDropdownPanel = new JPanel();
        folderDropdownPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        folderDropdownPanel.add(new JLabel("Install to: "));
        folderDropdown = new JComboBox<>(new String[]{"Forge","Feather", "Custom"});
        folderDropdownPanel.add(folderDropdown);
        panel.add(folderDropdownPanel);

        // Install Button
        JButton installButton = new JButton("Install");
        installButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        installButton.addActionListener(e -> {
            String selectedVersion = (String) versionDropdown.getSelectedItem();
            String selectedFolder = (String) folderDropdown.getSelectedItem();
            if (selectedVersion != null && selectedFolder != null) {
                removeOldRelease(selectedFolder);
                installRelease(selectedVersion, selectedFolder);
            }
        });
        panel.add(installButton);

        // Set custom colors for buttons and background
        installButton.setBackground(new Color(0, 153, 204)); // Light Blue
        installButton.setForeground(Color.WHITE); // White text
        panel.setBackground(new Color(240, 240, 240)); // Light Gray background

        add(panel);

        fetchAllReleases();
    }

    private void fetchAllReleases() {
        String apiUrl = "https://api.github.com/repos/MrFast-js/SkyblockFeatures/releases";

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                try (InputStream inputStream = connection.getInputStream();
                     InputStreamReader reader = new InputStreamReader(inputStream);
                     BufferedReader bufferedReader = new BufferedReader(reader)) {

                    List<String> versions = parseReleases(bufferedReader);
                    updateVersionDropdown(versions);
                }
            } else {
                System.err.println("Failed to fetch releases. HTTP Status Code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> parseReleases(BufferedReader bufferedReader) {
        Gson gson = new Gson();
        List<String> versions = new ArrayList<>();

        JsonArray releasesArray = gson.fromJson(bufferedReader, JsonArray.class);
        for (JsonElement releaseElement : releasesArray) {
            JsonObject releaseObject = releaseElement.getAsJsonObject();
            String version = releaseObject.get("tag_name").getAsString();
            versions.add(version);
        }

        return versions;
    }

    private void updateVersionDropdown(List<String> versions) {
        SwingUtilities.invokeLater(() -> {
            versionDropdown.removeAllItems();
            for (String version : versions) {
                versionDropdown.addItem(version);
            }
        });
    }
    /**
     * Taken and modified from NotEnoughUpdates under GNU Lesser General Public License v3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/COPYING
     * @author Moulberry
     */
    private void removeOldRelease(String selectedFolder) {
        String minecraftDir = System.getenv("APPDATA");
        File destinationFolder = getDefaultFolder(selectedFolder, minecraftDir);
        if(destinationFolder.isDirectory()) {
            for (File file : destinationFolder.listFiles()) {
                if(file.getPath().endsWith(".jar")) {
                    try {
                        JarFile jarFile = new JarFile(file);
                        ZipEntry mcModInfo = jarFile.getEntry("mcmod.info");
                        if (mcModInfo != null) {
                            InputStream inputStream = jarFile.getInputStream(mcModInfo);
                            String modID = getModIDFromInputStream(inputStream);
                            if (modID.equals("skyblockfeatures")) {
                                jarFile.close();
                                try {
                                    boolean deleted = file.delete();
                                    if (!deleted) {
                                        throw new Exception();
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    showMessage("Was not able to delete the other Skyblock Features from your mods folder!" +
                                            System.lineSeparator() +
                                            "Please make sure that your minecraft is currently closed and try again, or feel" +
                                            System.lineSeparator() +
                                            "free to open your mods folder and delete those files manually.");
                                }
                                continue;
                            }
                        }
                        jarFile.close();
                    } catch (Exception e) {

                    }
                }
            }
        }
    }

    /**
     * Taken from NotEnoughUpdates under GNU Lesser General Public License v3.0
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/COPYING
     * @author Moulberry
     */
    private String getModIDFromInputStream(InputStream inputStream) {
        String version = "";
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((version = bufferedReader.readLine()) != null) {
                if (version.contains("\"modid\": \"")) {
                    version = version.split(Pattern.quote("\"modid\": \""))[1];
                    version = version.substring(0, version.length() - 2);
                    break;
                }
            }
        } catch (Exception ex) {
            // RIP, couldn't find the modid...
        }
        return version;
    }


    private void installRelease(String version, String selectedFolder) {
        String minecraftDir = System.getenv("APPDATA");
        File destinationFolder = getDefaultFolder(selectedFolder, minecraftDir);

        try {
            URL downloadUrl = getDownloadUrl(version);
            if (downloadUrl != null) {
                System.out.println("DWONALODAING "+downloadUrl);
                String fileName = downloadUrl.toString().split("/")[downloadUrl.toString().split("/").length-1];
                Path destinationPath = destinationFolder.toPath().resolve(fileName);
                Files.copy(downloadUrl.openStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                showMessage("Installation successful!");
            } else {
                showMessage("Failed to get download URL. Installation aborted.");
            }
        } catch (IOException e) {
            showMessage("Error copying file: " + e.getMessage());
        }
    }

    private URL getDownloadUrl(String version) throws IOException {
        String apiUrl = "https://api.github.com/repos/MrFast-js/SkyblockFeatures/releases/tags/" + version;
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            try (InputStream inputStream = connection.getInputStream();
                 InputStreamReader reader = new InputStreamReader(inputStream);
                 BufferedReader bufferedReader = new BufferedReader(reader)) {

                Gson gson = new Gson();
                JsonObject releaseObject = gson.fromJson(bufferedReader, JsonObject.class);
                JsonArray assets = releaseObject.getAsJsonArray("assets");

                if (assets.size() > 0) {
                    JsonObject asset = assets.get(0).getAsJsonObject();
                    String downloadUrl = asset.get("browser_download_url").getAsString();
                    return new URL(downloadUrl);
                }
            }
        } else {
            System.err.println("Failed to fetch release info. HTTP Status Code: " + responseCode);
        }

        return null;
    }

    private File getDefaultFolder(String option, String minecraftDir) {
        switch (option) {
            case "Feather":
                return new File(minecraftDir + "\\.feather\\user-mods");
            case "Forge":
                return new File(minecraftDir + "\\.minecraft\\mods");
            case "Custom":
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int result = fileChooser.showOpenDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    return fileChooser.getSelectedFile();
                } else {
                    showMessage("Installation cancelled.");
                    System.exit(0);
                }
            default:
                return null;
        }
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                InstallerFrame installer = new InstallerFrame();
                installer.setVisible(true);
            }
        });
    }
}
