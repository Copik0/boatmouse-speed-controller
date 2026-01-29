import com.sun.jna.*;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;

public class BoatMouse {
    /* ================= JNA ================= */

    public interface User32 extends StdCallLibrary {
        User32 INSTANCE = Native.load("user32", User32.class);
        int SPI_SETMOUSESPEED = 0x0071;
        int SPI_SETMOUSE = 0x0004;

        boolean SystemParametersInfoW(int uiAction, int uiParam, int pvParam, int fWinIni);
        boolean SystemParametersInfoW(int uiAction, int uiParam, int[] pvParam, int fWinIni);
    }

    private static void setMouseSpeed(int speed) {
        User32.INSTANCE.SystemParametersInfoW(0x0071, 0, speed, 0x01 | 0x02);
    }

    private static void setMouseAcceleration(boolean enabled) {
        int[] params = enabled ? new int[]{6, 10, 1} : new int[]{0, 0, 0};
        User32.INSTANCE.SystemParametersInfoW(0x0004, 0, params, 0x01 | 0x02);
    }

    /* ================= CONFIG ================= */

    private static final File CONFIG = new File(System.getProperty("user.home"), ".boatmouse.properties");
    private static int slow = 3;
    private static int fast = 10;
    private static int startup = 10;
    private static boolean setOnStartup = false;
    private static boolean accelerationEnabled = false; 

    private static void loadConfig() {
        if (!CONFIG.exists()) return;
        try (FileInputStream in = new FileInputStream(CONFIG)) {
            Properties p = new Properties();
            p.load(in);
            slow = Integer.parseInt(p.getProperty("slow", "3"));
            fast = Integer.parseInt(p.getProperty("fast", "10"));
            startup = Integer.parseInt(p.getProperty("startup", "10"));
            setOnStartup = Boolean.parseBoolean(p.getProperty("startupEnabled", "false"));
            accelerationEnabled = Boolean.parseBoolean(p.getProperty("acceleration", "false"));
        } catch (Exception ignored) {}
    }

    private static void saveConfig() {
        try (FileOutputStream out = new FileOutputStream(CONFIG)) {
            Properties p = new Properties();
            p.setProperty("slow", String.valueOf(slow));
            p.setProperty("fast", String.valueOf(fast));
            p.setProperty("startup", String.valueOf(startup));
            p.setProperty("startupEnabled", String.valueOf(setOnStartup));
            p.setProperty("acceleration", String.valueOf(accelerationEnabled));
            p.store(out, "BoatMouse config");
        } catch (Exception ignored) {}
    }

    /* ================= UI STYLES ================= */

    private static final Color BG = new Color(25, 25, 25);
    private static final Color FG = new Color(220, 220, 220);
    private static final Color BTN = new Color(55, 55, 55);
    private static final Color BTN_FOCUS = new Color(80, 80, 100);
    private static final Color BAR = new Color(18, 18, 18);

    private static void makeFocusable(JButton b, Color normalBg) {
        b.setFocusable(true);
        b.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { b.setBackground(BTN_FOCUS); }
            public void focusLost(FocusEvent e) { b.setBackground(normalBg); }
        });
    }

    private static JButton button(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(260, 48)); 
        b.setMaximumSize(new Dimension(260, 48));
        b.setBackground(BTN);
        b.setForeground(FG);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        makeFocusable(b, BTN);
        return b;
    }

    /* ================= SETTINGS ================= */

    private static void openSettings(JFrame parent) {
        JDialog d = new JDialog(parent, "Settings", true);
        d.setSize(380, 420);
        d.setResizable(false);
        d.setLocationRelativeTo(parent);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Создаем спиннеры
        JSpinner sSlow = new JSpinner(new SpinnerNumberModel(slow, 1, 20, 1));
        JSpinner sFast = new JSpinner(new SpinnerNumberModel(fast, 1, 20, 1));
        JSpinner sStart = new JSpinner(new SpinnerNumberModel(startup, 1, 20, 1));

        addSettingRow(p, "Slow speed (1-20):", sSlow);
        addSettingRow(p, "Fast speed (1-20):", sFast);
        addSettingRow(p, "Startup speed (1-20):", sStart);

        p.add(Box.createVerticalStrut(10));

        JCheckBox cbStart = new JCheckBox("Set speed on startup", setOnStartup);
        JCheckBox cbAccel = new JCheckBox("Enable Mouse Acceleration", accelerationEnabled);

        for (JCheckBox cb : new JCheckBox[]{cbStart, cbAccel}) {
            cb.setBackground(BG);
            cb.setForeground(FG);
            cb.setFocusPainted(false);
            cb.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(cb);
            p.add(Box.createVerticalStrut(10));
        }

        JButton save = button("Save");
        save.setAlignmentX(Component.LEFT_ALIGNMENT);
        save.addActionListener(e -> {
            // Теперь данные реально считываются из UI
            slow = (int) sSlow.getValue();
            fast = (int) sFast.getValue();
            startup = (int) sStart.getValue();
            setOnStartup = cbStart.isSelected();
            accelerationEnabled = cbAccel.isSelected();

            setMouseAcceleration(accelerationEnabled);
            saveConfig();
            updateMainButtons();
            d.dispose();
        });

        p.add(Box.createVerticalGlue());
        p.add(save);

        d.setContentPane(p);
        d.setVisible(true);
    }

    private static void addSettingRow(JPanel parent, String text, JSpinner spinner) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG);
        row.setMaximumSize(new Dimension(400, 35));
        JLabel l = new JLabel(text);
        l.setForeground(FG);
        row.add(l, BorderLayout.WEST);
        spinner.setPreferredSize(new Dimension(60, 25));
        row.add(spinner, BorderLayout.EAST);
        parent.add(row);
        parent.add(Box.createVerticalStrut(10));
    }

    private static JButton slowBtn, fastBtn;

    private static void updateMainButtons() {
        SwingUtilities.invokeLater(() -> {
            slowBtn.setText("Slow (" + slow + ")");
            fastBtn.setText("Fast (" + fast + ")");
        });
    }

    /* ================= MAIN ================= */

    public static void main(String[] args) {
        loadConfig();
        if (setOnStartup) setMouseSpeed(startup);
        setMouseAcceleration(accelerationEnabled);

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setUndecorated(true);
            f.setSize(350, 320); 
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(BG);

            // Title Bar
            JPanel bar = new JPanel(new BorderLayout());
            bar.setBackground(BAR);
            bar.setPreferredSize(new Dimension(0, 32));

            JLabel title = new JLabel("  BoatMouse");
            title.setForeground(FG);

            JPanel winBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            winBtns.setBackground(BAR);

            JButton min = new JButton("-");
            JButton cls = new JButton("X");

            for (JButton b : new JButton[]{min, cls}) {
                b.setPreferredSize(new Dimension(45, 32));
                b.setForeground(FG);
                b.setBackground(BAR);
                b.setBorderPainted(false);
                b.setFocusPainted(false);
                makeFocusable(b, BAR);
            }

            min.addActionListener(e -> f.setState(Frame.ICONIFIED));
            cls.addActionListener(e -> System.exit(0));
            winBtns.add(min); winBtns.add(cls);
            bar.add(title, BorderLayout.WEST);
            bar.add(winBtns, BorderLayout.EAST);

            MouseAdapter drag = new MouseAdapter() {
                Point start;
                public void mousePressed(MouseEvent e) { start = e.getPoint(); }
                public void mouseDragged(MouseEvent e) {
                    Point p = e.getLocationOnScreen();
                    f.setLocation(p.x - start.x, p.y - start.y);
                }
            };
            bar.addMouseListener(drag);
            bar.addMouseMotionListener(drag);

            // Content
            JPanel c = new JPanel();
            c.setBackground(BG);
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
            c.setBorder(new EmptyBorder(30, 20, 30, 20));

            slowBtn = button("Slow (" + slow + ")");
            fastBtn = button("Fast (" + fast + ")");
            JButton settings = button("Settings");

            slowBtn.addActionListener(e -> setMouseSpeed(slow));
            fastBtn.addActionListener(e -> setMouseSpeed(fast));
            settings.addActionListener(e -> openSettings(f));

            c.add(slowBtn); c.add(Box.createVerticalStrut(15));
            c.add(fastBtn); c.add(Box.createVerticalStrut(15));
            c.add(settings);

            root.add(bar, BorderLayout.NORTH);
            root.add(c, BorderLayout.CENTER);
            f.setContentPane(root);
            f.setVisible(true);
        });
    }
}
