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
        int SPI_GETMOUSESPEED = 0x0070;

        boolean SystemParametersInfoW(
                int uiAction,
                int uiParam,
                WinDef.UINTByReference pvParam,
                 int fWinIni
        );
        boolean SystemParametersInfoW(
                int uiAction,
                int uiParam,
                int pvParam,
                 int fWinIni
        );
    }

    private static void setMouseSpeed(int speed) {
        User32.INSTANCE.SystemParametersInfoW(
                User32.SPI_SETMOUSESPEED,
                0,
                 speed,
                0x01 | 0x02 // SPIF_UPDATEINIFILE | SPIF_SENDCHANGE
        );
    }

    private static int getCurrentMouseSpeed() {
        WinDef.UINTByReference ref = new WinDef.UINTByReference(new WinDef.UINT(10));
        User32.INSTANCE.SystemParametersInfoW(User32.SPI_GETMOUSESPEED, 0, ref, 0);
        return ref.getValue().intValue();
    }

    /* ================= CONFIG ================= */

    private static final File CONFIG =
            new File(System.getProperty("user.home"), ".boatmouse.properties");

    private static int slow = 3;
    private static int fast = 10;
    private static int startup = 10;
    private static boolean setOnStartup = false;

    private static void loadConfig() {
         if (!CONFIG.exists()) return;
        try (FileInputStream in = new FileInputStream(CONFIG)) {
            Properties p = new Properties();
            p.load(in);
            slow = Integer.parseInt(p.getProperty("slow", "3"));
            fast = Integer.parseInt(p.getProperty("fast", "10"));
            startup = Integer.parseInt(p.getProperty("startup", "10"));
            setOnStartup = Boolean.parseBoolean(p.getProperty("startupEnabled", "false"));
        } catch (Exception ignored) {}
    }

    private static void saveConfig() {
        try (FileOutputStream out = new FileOutputStream(CONFIG)) {
            Properties p = new Properties();
            p.setProperty("slow", String.valueOf(slow));
            p.setProperty("fast", String.valueOf(fast));
            p.setProperty("startup", String.valueOf(startup));
            p.setProperty("startupEnabled", String.valueOf(setOnStartup));
            p.store(out, "BoatMouse config");
        } catch (Exception ignored) {}
    }

    /* ================= UI COLORS ================= */

    private static final Color BG = new Color(25, 25, 25);
    private static final Color FG  = new Color(220, 220, 220);
    private static final Color BTN = new Color(55, 55, 55);
    private static final Color BAR = new Color(18, 18, 18);

    private static JButton button(String text) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(260, 48));
        b.setBackground(BTN);
        b.setForeground(FG);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 16f));
        b.setFocusPainted(false);
        b.setAlignmentX(Component.CENTER_ALIGNMENT); // Центрирование
        return b;
    }

    /* ================= SETTINGS ================= */

    private static void openSettings(JFrame parent) {
        JDialog d =  new JDialog(parent, "Settings", true);
        d.setSize(400, 300);
        d.setResizable(false);
        d.setLocationRelativeTo(parent);

        JPanel p = new JPanel(new GridLayout(5, 2, 10, 10));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel l1 = new JLabel("Slow speed (1–20)");
        JLabel l2 = new JLabel("Fast speed (1–20)");
        JLabel l3 = new JLabel("Startup speed (1–20)");

        for (JLabel l : new JLabel[]{l1, l2, l3}) {
            l.setForeground(FG);
        }

        JSpinner s1 = new JSpinner(new SpinnerNumberModel(slow, 1, 20, 1));
        JSpinner s2 =  new JSpinner(new SpinnerNumberModel(fast, 1, 20, 1));
        JSpinner s3 = new JSpinner(new SpinnerNumberModel(startup, 1, 20, 1));

        JCheckBox cb = new JCheckBox("Set speed on startup", setOnStartup);
        cb.setBackground(BG);
        cb.setForeground(FG);

        JButton save = button("Save");

        save.addActionListener(e -> {
            slow = (int) s1.getValue();
            fast = (int) s2.getValue();
            startup = (int) s3.getValue();
            setOnStartup = cb.isSelected();
            saveConfig();

            // Обновляем текст на кнопках в главном окне
            updateMainButtons();

             d.dispose();
        });

        p.add(l1); p.add(s1);
        p.add(l2); p.add(s2);
        p.add(l3); p.add(s3);
        p.add(new JLabel()); p.add(cb);
        p.add(new JLabel()); p.add(save);

         d.setContentPane(p);
        d.setVisible(true);
    }

    // Новые глобальные переменные для доступа к кнопкам
    private static JButton slowBtn;
    private static JButton fastBtn;

    private static void updateMainButtons() {
        SwingUtilities.invokeLater(() -> {
            slowBtn.setText("🐢 Slow (" + slow + ")");
            fastBtn.setText("🐇 Fast (" + fast + ")");
        });
    }

    /* ================= MAIN ================= */

    public static void main(String[] args) {
        loadConfig();
        if (setOnStartup)  setMouseSpeed(startup);

        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setUndecorated(true);
            f.setSize(350, 250);
            f.setLocationRelativeTo(null);
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel root = new JPanel();
            root.setBackground(BG);
            root.setLayout(new BorderLayout());

            /* ---- Title Bar ---- */
            JPanel bar = new JPanel(new BorderLayout());
            bar.setBackground(BAR);
            bar.setPreferredSize(new Dimension(0, 32));

            JLabel title = new JLabel("  BoatMouse");
            title.setForeground(FG);

            JButton close = new JButton("✕");
            close.setForeground(FG);
            close.setBackground(BAR);
            close.setFocusPainted(false);
            close.addActionListener(e -> System.exit(0));

            bar.add(title, BorderLayout.WEST);
            bar.add(close, BorderLayout.EAST);

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

            /* ---- Content ---- */
            JPanel c = new JPanel();
            c.setBackground(BG);
            c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); // Используем BoxLayout для центрирования
            c.setBorder(new EmptyBorder(20, 20, 20, 20));

            // Создаем кнопки как глобальные переменные
            slowBtn = button("🐢 Slow (" + slow + ")");
            fastBtn = button("🐇 Fast (" + fast + ")");
            JButton settings = button("⚙ Settings");

            slowBtn.addActionListener(e -> setMouseSpeed(slow));
            fastBtn.addActionListener(e -> setMouseSpeed(fast));
            settings.addActionListener(e -> openSettings(f));

            // Добавляем компоненты с центрированием
            c.add(slowBtn);
            c.add(Box.createVerticalStrut(15));
            c.add(fastBtn);
            c.add(Box.createVerticalStrut(20));
            c.add(settings);

            root.add(bar, BorderLayout.NORTH);
            root.add(c, BorderLayout.CENTER);

            f.setContentPane(root);
            f.setVisible(true);
        });
    }
}