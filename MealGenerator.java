import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import java.util.HashMap;

public class MealGenerator extends JFrame 
{
    private JComboBox<String> regionCombo,dietCombo;
    private JTextField caloriesField;
    private JSlider[]nutritionSliders;
    private JCheckBox selectAllCheckbox;
    private JPanel selectAllPanel;
    private JCheckBox[]ingredientCheckboxes;
    private JTextArea mealsArea;
    private Connection conn;
    private JPanel ingredientsPanel;
    private Map<String,List<JCheckBox>> ingredientGroups=new HashMap<>();
    private final String[]regions={"Global","South India","North India","Middle East","South America","North America","Africa","England","France","Italy","Spain"};
    private final String[]diets={"Vegan","Vegetarian","Keto","Low-Glycemic","Weight-Gain","Clean Eating","Slow Food"};
    private final String[]nutrients={"Carbs","Protein","Fat","Sugar","Fiber","Vitamins"};

    public MealGenerator() 
    {
        setTitle("Meals Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800,600);
        setLayout(new BorderLayout());
        ImageIcon icon=new ImageIcon("app_icon.png");
        setIconImage(icon.getImage());
        setVisible(true);
        initDatabase();
        JTabbedPane tabbedPane=new JTabbedPane();
        JPanel generatePanel=createGeneratePanel();
        JPanel mealsPanel=createMealsPanel();
        tabbedPane.addTab("Generate Meals",generatePanel);
        tabbedPane.addTab("Meals",mealsPanel);
        add(tabbedPane,BorderLayout.CENTER);
        loadIngredients();
    }

    private void initDatabase() 
    {
        try 
            {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url="jdbc:mysql://localhost:3306/meal_generator?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
            String user="********";//Input your own MySQL username
            String password="*************";//Input your own MySQL password
            conn=DriverManager.getConnection(url,user,password);
        } 
        catch(ClassNotFoundException|SQLException e) 
            {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Database connection failed: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private JPanel createGeneratePanel() 
    {
        JPanel panel=new JPanel(new BorderLayout());
        JPanel formPanel=new JPanel(new GridBagLayout());
        GridBagConstraints gbc=new GridBagConstraints();
        gbc.insets=new Insets(5,5,5,5);
        gbc.fill=GridBagConstraints.HORIZONTAL;
        gbc.gridx=0;
        gbc.gridy=0;
        formPanel.add(new JLabel("Region:"),gbc);
        gbc.gridx=1;
        regionCombo=new JComboBox<>(regions);
        formPanel.add(regionCombo,gbc);
        gbc.gridx=0;
        gbc.gridy=1;
        formPanel.add(new JLabel("Diet:"),gbc);
        gbc.gridx=1;
        dietCombo=new JComboBox<>(diets);
        formPanel.add(dietCombo,gbc);
        gbc.gridx=0;
        gbc.gridy=2;
        formPanel.add(new JLabel("Calories(500-2500):"),gbc);
        gbc.gridx=1;
        caloriesField=new JTextField("1500",10);
        formPanel.add(caloriesField,gbc);
        nutritionSliders=new JSlider[nutrients.length];
        JPanel nutritionPanel=new JPanel(new GridLayout(nutrients.length,3,10,5));
        for(int i=0;i<nutrients.length;i++) 
        {
            JLabel label=new JLabel(nutrients[i]+ "(%):");
            JSlider slider=new JSlider(0,100,50);
            slider.setMajorTickSpacing(25);
            slider.setPaintTicks(true);
            slider.setPaintLabels(true);
            JLabel valueLabel=new JLabel("50%");
            slider.addChangeListener(e-> valueLabel.setText(slider.getValue()+"%"));
            nutritionSliders[i]= slider;
            nutritionPanel.add(label);
            nutritionPanel.add(slider);
            nutritionPanel.add(valueLabel);
        }
        ingredientsPanel=new JPanel(new GridLayout(0,2));
        ingredientCheckboxes=new JCheckBox[0];
        JScrollPane nutritionScroll=new JScrollPane(nutritionPanel);
        JScrollPane ingredientsScroll=new JScrollPane(ingredientsPanel);
        JPanel inputPanel=new JPanel(new BorderLayout());
        inputPanel.add(formPanel,BorderLayout.NORTH);
        inputPanel.add(nutritionScroll,BorderLayout.CENTER);
        JPanel selectAllPanel=new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectAllCheckbox=new JCheckBox("Select All Ingredients");
        selectAllCheckbox.addActionListener(e-> {
            boolean selected=selectAllCheckbox.isSelected();
            for(JCheckBox cb:ingredientCheckboxes) 
            {
                cb.setSelected(selected);
            }
        });
        selectAllPanel.add(new JLabel("Exclude Ingredients:"));
        selectAllPanel.add(selectAllCheckbox);
        inputPanel.add(selectAllPanel,BorderLayout.SOUTH);
        panel.add(inputPanel,BorderLayout.NORTH);
        panel.add(ingredientsScroll,BorderLayout.CENTER);
        JButton generateButton=new JButton("Generate Meals");
        generateButton.addActionListener(e-> generateMeals());
        panel.add(generateButton,BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMealsPanel() 
    {
        JPanel panel=new JPanel(new BorderLayout());
        mealsArea=new JTextArea(20,50);
        mealsArea.setEditable(false);
        JScrollPane scrollPane=new JScrollPane(mealsArea);
        panel.add(scrollPane,BorderLayout.CENTER);
        return panel;
    }

    private void loadIngredients() 
    {
        try 
            {
            ingredientGroups.clear();
            Statement stmt=conn.createStatement();
            ResultSet rs=stmt.executeQuery("SELECT ingredient_id,ingredient_name,food_group,calories_per_100g FROM Ingredients");
            while(rs.next()) 
            {
                int id=rs.getInt("ingredient_id");
                String name=rs.getString("ingredient_name");
                String group=rs.getString("food_group");
                int calories=rs.getInt("calories_per_100g");

                JCheckBox cb=new JCheckBox(name+"("+calories+" cal/100g)");
                cb.setActionCommand(String.valueOf(id));
                ingredientGroups.computeIfAbsent(group,k-> new ArrayList<>()).add(cb);
            }

            ingredientsPanel.removeAll();
            ingredientsPanel.setLayout(new BoxLayout(ingredientsPanel,BoxLayout.Y_AXIS));

            for(Map.Entry<String,List<JCheckBox>> entry:ingredientGroups.entrySet()) 
            {
                JPanel groupPanel=new JPanel(new GridLayout(0,2));
                groupPanel.setBorder(BorderFactory.createTitledBorder(entry.getKey()));
                for(JCheckBox cb:entry.getValue()) 
                {
                    groupPanel.add(cb);
                }
                ingredientsPanel.add(groupPanel);
            }

            ingredientsPanel.revalidate();
            ingredientsPanel.repaint();

            // Flatten into a single array for "select all" logic
            ingredientCheckboxes=ingredientGroups.values().stream().flatMap(List::stream).toArray(JCheckBox[]::new);

        } 
        catch(SQLException e) 
            {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Failed to load ingredients: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateMeals() 
    {
        try 
            {
            // Validate calories
            int calories;
            try 
                {
                calories=Integer.parseInt(caloriesField.getText());
                if(calories<500 || calories>2500) 
                {
                    throw new NumberFormatException("Calories must be between 500 & 2500");
                }
            } 
            catch(NumberFormatException e) 
                {
                JOptionPane.showMessageDialog(this,"Invalid calories: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Map regions and diets
            Map<String,Integer> regionMap=new HashMap<>();
            regionMap.put("Global",1);
            regionMap.put("South India",2);
            regionMap.put("North India",3);
            regionMap.put("Middle East",4);
            regionMap.put("South America",5);
            regionMap.put("North America",6);
            regionMap.put("Africa",7);
            regionMap.put("England",8);
            regionMap.put("France",9);
            regionMap.put("Italy",10);
            regionMap.put("Spain",11);

            Map<String,Integer> dietMap=new HashMap<>();
            dietMap.put("Vegan",1);
            dietMap.put("Vegetarian",2);
            dietMap.put("Keto",3);
            dietMap.put("Low-Glycemic",4);
            dietMap.put("Weight-Gain",5);
            dietMap.put("Clean Eating",6);
            dietMap.put("Slow Food",7);

            int regionId=regionMap.get(regionCombo.getSelectedItem());
            int dietId=dietMap.get(dietCombo.getSelectedItem());

            // Get nutrition slider upper bounds(relaxed to 100g)
            int[]nutritionValues=new int[nutritionSliders.length];
            for(int i=0;i<nutritionSliders.length;i++) 
            {
                nutritionValues[i]= nutritionSliders[i].getValue() * 2;// Scale to 0-200g
            }

            // Get selected ingredients to exclude
            List<Integer> excludeIds=new ArrayList<>();
            for(JCheckBox cb:ingredientCheckboxes) 
            {
                if(cb.isSelected()) 
                {
                    excludeIds.add(Integer.parseInt(cb.getActionCommand()));
                }
            }

            // Begin SQL query
            StringBuilder queryBuilder=new StringBuilder("SELECT DISTINCT m.* FROM Meals m ");

            // Add exclusion clause
            if(!excludeIds.isEmpty()) 
            {
                queryBuilder.append("WHERE NOT EXISTS(SELECT 1 FROM Meal_ingredients mi ")
                            .append("WHERE mi.meal_id=m.meal_id AND mi.ingredient_id IN(");
                for(int i=0;i<excludeIds.size();i++) 
                {
                    queryBuilder.append("?");
                    if(i<excludeIds.size() - 1) queryBuilder.append(",");
                }
                queryBuilder.append(")) ");
            } 
            else 
            {
                queryBuilder.append("WHERE 1=1 ");
            }

            // Add region,diet,and calorie filters
            queryBuilder.append("AND m.region_id=? ");
            queryBuilder.append("AND m.diet_id=? ");
            queryBuilder.append("AND m.calories <= ? ");

            // Add nutrition filters(optional,for debugging)
            for(String nutrient:nutrients) 
            {
                String col=nutrient.equalsIgnoreCase("fiber") ? "fiber":nutrient.toLowerCase();
                queryBuilder.append(String.format("AND m.%s <= ? ",col));
            }

            queryBuilder.append("LIMIT 20");

            // Prepare and bind parameters
            PreparedStatement pstmt=conn.prepareStatement(queryBuilder.toString());
            int paramIndex=1;
            for(int id:excludeIds) 
            {
                pstmt.setInt(paramIndex++,id);
            }
            pstmt.setInt(paramIndex++,regionId);
            pstmt.setInt(paramIndex++,dietId);
            pstmt.setInt(paramIndex++,calories);

            for(int val:nutritionValues) 
            {
                pstmt.setInt(paramIndex++,val);
            }

            // Debug
            System.out.println("Final SQL: "+queryBuilder);
            System.out.println("Excluded ingredient IDs: "+excludeIds);
            System.out.println("Region ID: "+regionId+",Diet ID: "+dietId+",Max Calories: "+calories);
            System.out.println("Nutrition upper bounds: "+Arrays.toString(nutritionValues));

            // Execute and display
            ResultSet rs=pstmt.executeQuery();
            displayMeals(rs,excludeIds.size());

        } 
        catch(SQLException e) 
            {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Failed to generate meals: "+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayMeals(ResultSet rs,int ingredientCount) throws SQLException 
    {
        StringBuilder mealsText=new StringBuilder();
        int count=0;
        while(rs.next()) 
        {
            System.out.println("Found meal: "+rs.getString("name")+",ID: "+rs.getInt("meal_id"));
            mealsText.append("Meal: ").append(rs.getString("name")).append("\n");
            mealsText.append("Calories: ").append(rs.getInt("calories")).append(" kcal\n");
            mealsText.append("Carbs: ").append(rs.getInt("carbs")).append(" g\n");
            mealsText.append("Protein: ").append(rs.getInt("protein")).append(" g\n");
            mealsText.append("Fat: ").append(rs.getInt("fat")).append(" g\n");
            mealsText.append("Sugar: ").append(rs.getInt("sugar")).append(" g\n");
            mealsText.append("Fiber: ").append(rs.getInt("fiber")).append(" g\n");
            mealsText.append("Vitamins: ").append(rs.getInt("vitamins")).append(" %\n");
            mealsText.append("Cooking Time: ").append(rs.getInt("cooking_time")).append(" minutes\n\n");
            count++;
        }
        System.out.println("Total meals found: "+count);
        if(count==0) 
        {
            String message=ingredientCount==0 ?
                "No meals found in the database." :
                "No meals found which excludes the selected ingredient(s).";
            mealsArea.setText(message);
        } 
        else 
        {
            mealsArea.setText(mealsText.toString());
        }
    }

    @Override
    public void dispose() 
    {
        try 
            {
            if(conn != null && !conn.isClosed()) 
            {
                conn.close();
            }
        } 
        catch(SQLException e) 
            {
            e.printStackTrace();
        }
        super.dispose();
    }

    public static void main(String[]args) 
    {
        SwingUtilities.invokeLater(()-> {
            try 
                {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new MealGenerator().setVisible(true);
            } 
            catch(Exception e) 
                {
                e.printStackTrace();
            }
        });
    }
}
