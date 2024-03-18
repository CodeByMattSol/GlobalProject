package delegate;

import model.Board;
import model.Game;

import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class RenjuGUI {
    // Поле для хранения игровой логики (модель)
    private Game game;
    // Главное окно приложения
    private JFrame gameFrame;
    // Стартовое окно приложения
    private StartFrame startFrame;
    // Панель для хранения игрового поля
    private JPanel btPanel;
    // Игровое поле в виде двумерного массива кнопок
    private JButton[][] board;
    // Панель управления
    private JPanel ctrlPanel;
    // Метка первого игрока
    private JLabel player1NameLabel;
    // Метка второго игрока
    private JLabel player2NameLabel;
    // Кнопка "Сдаться"
    private JButton giveUpBt;
    // Кнопка перезапуска игры
    private JButton restartBt;
    // Фоновая музыка
    private Clip ambientClip;
    // Звук окончания игры
    private Clip endGameClip;
    // Звук совершения хода
    private Clip hitBtClip;
    // Стилистика приложения
    private Color greenColor = new Color(50,205,50);
    private Color redColor = new Color(200,34,34);
    private Border blackBorder = BorderFactory.createLineBorder(Color.black,5,false);
    private final Font mainFont = new Font("Comic Sans MS", Font.BOLD, 22);
    // Конструктор
    public RenjuGUI() {
        // Настраиваем главное окно приложения
        gameFrame = new JFrame();
        gameFrame.setTitle("Рэндзю");
        gameFrame.setLayout(new FlowLayout(FlowLayout.CENTER));
        gameFrame.setSize(1250, 950);
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Первоначально деактивируем окно
        gameFrame.setEnabled(false);
        // Настраиваем панель с игровым поле
        btPanel = new JPanel();
        btPanel.setLayout(new GridLayout(15,15));
        btPanel.setPreferredSize(new Dimension(900,900));
        initBoard(); // Настраиваем игровое поле
        initClips(); // Настраиваем звук
        // Настраиваем панель управления
        ctrlPanel = new JPanel();
        ctrlPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0,5));
        ctrlPanel.setPreferredSize(new Dimension(300,900));
        // Настраиваем метку первого игрока
        player1NameLabel = new JLabel("X: ");
        player1NameLabel.setPreferredSize(new Dimension(250,100));
        player1NameLabel.setFont(mainFont);
        player1NameLabel.setBorder(blackBorder);
        player1NameLabel.setOpaque(true);
        ctrlPanel.add(player1NameLabel);
        // Настраиваем метку второго игрока
        player2NameLabel = new JLabel("O: ");
        player2NameLabel.setPreferredSize(new Dimension(250,100));
        player2NameLabel.setFont(mainFont);
        player2NameLabel.setBorder(blackBorder);
        player2NameLabel.setOpaque(true);
        ctrlPanel.add(player2NameLabel);
        // Настраиваем кнопку "Сдаться"
        giveUpBt = new JButton("Сдаться");
        giveUpBt.setFont(mainFont);
        giveUpBt.setFocusable(false);
        giveUpBt.setBackground(Color.white);
        giveUpBt.setPreferredSize(new Dimension(250,100));
        giveUpBt.addActionListener(e -> {
            // Сдавшийся игрок будет выделен красным цветом
            if (game.getCurrentPlayer().getSymbol() == 'X') {
                player1NameLabel.setBackground(redColor);
            } else {
                player2NameLabel.setBackground(redColor);
            }
            // Запускаем логику конца игры
            endGame(game.showResult());
        });
        ctrlPanel.add(giveUpBt);
        // Настраиваем кнопку перезапуска игры
        restartBt = new JButton("Начать новую игру");
        restartBt.setFont(mainFont);
        restartBt.setFocusable(false);
        restartBt.setBackground(Color.white);
        restartBt.setPreferredSize(new Dimension(250,100));
        // При нажатии на кнопку просто запускаем логику конца игры
        restartBt.addActionListener(e -> restartGame());
        ctrlPanel.add(restartBt);
        // Добавляем панели на фрейм и делаем его видимым
        gameFrame.add(btPanel);
        gameFrame.add(ctrlPanel);
        gameFrame.setVisible(true);
        // Запускаем фоновую музыку
        if (ambientClip != null) {
            ambientClip.start();
        }
        // Запускаем стартовое окно
        startFrame = new StartFrame();
    }
    // Вспомогательный метод для обработки игрового поля
    private void forEachBoardBt(Consumer<JButton> action) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                action.accept(board[i][j]);
            }
        }
    }
    // Метод инициализации игрового поля
    private void initBoard() {
        // Выделяем память для всего массива
        board = new JButton[15][15];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                // Выделяем память для каждого элемента массива
                JButton curBt = board[i][j] = new JButton("");
                curBt.setBackground(Color.white);
                curBt.setFont(mainFont);
                curBt.setFocusable(false);
                curBt.addActionListener(new MakeTurnListener());
                btPanel.add(curBt);
            }
        }
    }
    // Метод отображения игрового поля согласно модели
    private void printBoard() {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                board[i][j].setText(String.valueOf(game.getBoard().getBoard()[i][j]));
            }
        }
    }
    // Метод инициализации звукового сопровождения
    private void initClips() {
        // Объявляем файлы
        File ambientFile = new File("ambient_music.wav");
        File endGameFile = new File("end_game.wav");
        File hitBtFile = new File("hit_bt.wav");
        try {
            // Объявляем потоки для чтения аудиофайлов
            AudioInputStream ambientStream = AudioSystem.getAudioInputStream(ambientFile);
            AudioInputStream endGameStream = AudioSystem.getAudioInputStream(endGameFile);
            AudioInputStream hitBtStream = AudioSystem.getAudioInputStream(hitBtFile);
            // Инициализируем клипы
            ambientClip = AudioSystem.getClip();
            endGameClip = AudioSystem.getClip();
            hitBtClip = AudioSystem.getClip();
            ambientClip.open(ambientStream);
            endGameClip.open(endGameStream);
            hitBtClip.open(hitBtStream);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            // Если что-то пошло не так, то сбрасываем значения ссылок
            ambientClip = null;
            endGameClip = null;
            hitBtClip = null;
        }

    }
    // Метод проверки хода
    private void checkTurn() {
        // Подгружаем сделанный ход из модели
        printBoard();
        // Проверяем условие конца игры
        if (game.isGameOver()) {
            // Запускаем логику конца игры
            endGame(game.showResult());
            return;
        }
        // Если игра не кончилась, то меняем игрока
        game.switchPlayer();
        // Меняем цвета игроков
        Color temp = player1NameLabel.getBackground();
        player1NameLabel.setBackground(player2NameLabel.getBackground());
        player2NameLabel.setBackground(temp);
    }
    // Метод конца игры
    private void endGame(String result) {
        // Воспроизводим звук конца игры
        if (endGameClip != null) {
            endGameClip.stop();
            endGameClip.setFramePosition(0);
            endGameClip.start();
        }
        // Выделяем цветом победную комбинацию
        for (Point p : game.getBoard().getWinCombination()) {
            int i = (int) p.getX();
            int j = (int) p.getY();
            board[i][j].setBackground(greenColor);
        }
        // Деактивируем игровое поле
        forEachBoardBt(bt -> {
            bt.setEnabled(false);
        });
        // Деактивируем кнопку "Сдаться"
        giveUpBt.setEnabled(false);
        // Выводим диалоговое окно с результатом игры
        JOptionPane.showMessageDialog(gameFrame, result,
                "Конец игры!", JOptionPane.INFORMATION_MESSAGE);
    }
    // Логика перезапуска игры
    private void restartGame() {
        // Сбрасываем игровое поле
        forEachBoardBt(curBt -> {
            curBt.setText("");
            curBt.setBackground(Color.white);
            curBt.setEnabled(true);
        });
        // Активируем кнопку сдаться
        giveUpBt.setEnabled(true);
        // Сбрасываем цвета игроков
        player1NameLabel.setBackground(null);
        player2NameLabel.setBackground(null);
        // Деактивируем главное окно
        gameFrame.setEnabled(false);
        // Запускаем стартовое окно
        startFrame = new StartFrame();
    }
    // Вспомогательный класс-слушатель игрового поля
    private class MakeTurnListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    // Находим нажатую кнопку
                    if (board[i][j] == e.getSource()) {
                        // Воспроизводим звук нажатия кнопки
                        if (hitBtClip != null) {
                            hitBtClip.stop();
                            hitBtClip.setFramePosition(0);
                            hitBtClip.start();
                        }
                        // Пробуем совершить ход
                        boolean result = game.getBoard().makeTurn(i,j,game.getCurrentPlayer().getSymbol());
                        // Если удалось совершить ход, то запускаем проверку
                        if (result) {
                            checkTurn();
                        }
                    }
                }
            }
        }
    }
    // Вспомогательный класс, описывающий стартовое окно приложения
    private class StartFrame extends JFrame {
        // Метка с приветствием и правилами игры
        private JLabel welcomeLabel;
        // Метки текстовых полей
        private JLabel player1Label;
        private JLabel player2Label;
        // Текстовые поля для ввода имён игроков
        private JTextField player1Field;
        private JTextField player2Field;
        // Кнопка для начал игры
        private JButton startButton;
        // Конструктор
        public StartFrame()  {
            // Настраиваем стартовое окно
            this.setTitle("Добро пожаловать!");
            this.setSize(800, 600);
            this.setLayout(new FlowLayout(FlowLayout.CENTER,0,20));
            // Делаем окно поверх других окон
            this.setAlwaysOnTop(true);
            this.setDefaultCloseOperation(EXIT_ON_CLOSE);
            this.setResizable(false);
            // Настраиваем метку с правилами
            welcomeLabel = new JLabel();
            // Для форматирования текста можно использовать язык разметки HTML
            welcomeLabel.setText("<html><div style='text-align: center;'>Добро пожаловать в игру Рэндзю!" +
                    "<br>Цель игры - собрать пять своих символов в ряд" +
                    "<br> по горизонтали, вертикали или диагонали." +
                    "<br>Игроки ходят по очереди, ставя крестики и " +
                    "<br>нолики на свободные клетки поля." +
                    "<br>Игра заканчивается ничьей, когда поле полностью заполнено," +
                    "<br>а выигрышная комбинация так и не была получена." +
                    "<br>" +
                    "<br>Желаем вам удачи и приятной игры!<br><br></div></html>");
            welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
            welcomeLabel.setFont(mainFont);
            // Настраиваем метку текстового поля
            player1Label = new JLabel("Имя первого игрока: ");
            player1Label.setPreferredSize(new Dimension(250,50));
            player1Label.setFont(mainFont);
            // Настраиваем текстовое поле
            player1Field = new JTextField();
            player1Field.setPreferredSize(new Dimension(350,50));
            player1Field.setFont(mainFont);
            // Настраиваем метку текстового поля
            player2Label = new JLabel("Имя второго игрока: ");
            player2Label.setPreferredSize(new Dimension(250,50));
            player2Label.setFont(mainFont);
            // Настраиваем текстовое поле
            player2Field = new JTextField();
            player2Field.setPreferredSize(new Dimension(350,50));
            player2Field.setFont(mainFont);
            // Настраиваем кнопку начала игры
            startButton = new JButton("Начать игру");
            startButton.setFont(mainFont);
            startButton.setPreferredSize(new Dimension(250,50));
            startButton.setFocusable(false);
            startButton.setBackground(greenColor);
            startButton.addActionListener(e -> {
                // Если введено пустое имя, то выводим сообщение
                if (player1Field.getText().equals("") || player2Field.getText().equals("")) {
                    JOptionPane.showMessageDialog(this,
                            "Имя игрока не может быть пустым!",
                            "Введите имена игроков", JOptionPane.WARNING_MESSAGE);
                } else {
                    // Сохраняем введённые имена игроков
                    String name1 = player1Field.getText();
                    String name2 = player2Field.getText();
                    // Инициализируем метки игроков
                    player1NameLabel.setText("X: " + name1);
                    player1NameLabel.setBackground(greenColor);
                    player2NameLabel.setText("O: " + name2);
                    player2NameLabel.setBackground(null);
                    // Создаём модель игры
                    game = new Game(name1, name2);
                    // Задаём символ для пустых клеток
                    game.getBoard().setEmpty(' ');
                    // Закрываем стартовое окно
                    this.dispose();
                    // Подгружаем игровое поле из модели
                    printBoard();
                    // Активируем и выводим главное окно приложения
                    gameFrame.setEnabled(true);
                    gameFrame.setVisible(true);
                }
            });
            // Добавляем компоненты на стартовое окно
            this.add(welcomeLabel);
            this.add(player1Label);
            this.add(player1Field);
            this.add(player2Label);
            this.add(player2Field);
            this.add(startButton);
            // Выставляем положение стартового окна по центру главного окна
            this.setLocationRelativeTo(gameFrame);
            this.setVisible(true);
        }
    }
}
