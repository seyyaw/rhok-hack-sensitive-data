
package org.rhok.refine.commands;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.google.refine.commands.Command;
import com.google.refine.ProjectManager;
import com.google.refine.model.Project;
import com.google.refine.model.ColumnModel;
import com.google.refine.model.Column;
import com.google.refine.model.Row;
import com.google.refine.browsing.Engine;
import com.google.refine.browsing.FilteredRows;
import com.google.refine.browsing.RowVisitor;
import com.google.refine.util.ParsingUtilities;

/**
 * 
 * Validates codice Fiscale in a column.
 * 
 */
public class ControlPrivacy extends Command {

    protected RowVisitor createRowVisitor(Project project, int cellIndex, List values)
            throws Exception {
        return new RowVisitor() {

            int cellIndex;
            List values;

            public RowVisitor init(int cellIndex, List<Float> values) {
                this.cellIndex = cellIndex;
                this.values = values;
                return this;
            }

            @Override
            public void start(Project project) {
                // nothing to do
            }

            @Override
            public void end(Project project) {
                // nothing to do
            }

            public boolean visit(Project project, int rowIndex, Row row) {
                try {
                    String val = (String) row.getCellValue(this.cellIndex);
                    this.values.add(val);
                } catch (Exception e) {
                }

                return false;
            }
        }.init(cellIndex, values);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doGet(request, response);
    };

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            ProjectManager.singleton.setBusy(true);
            Project project = getProject(request);
            ColumnModel columnModel = project.columnModel;
            int numberOfColumns = project.columnModel.columns.size();
            Map <String, ArrayList> sensitiveDatas = new HashMap<String, ArrayList>();
            ArrayList sensitiveData;
            for (int i = 0; i < numberOfColumns; i++) {
                Column mycolumn = project.columnModel.getColumnByCellIndex(i);
                sensitiveData = checkPrivacy(mycolumn, project, request);
                if(!(sensitiveData == null || sensitiveData.size() == 0)){
                    sensitiveDatas.put(mycolumn.getName(), sensitiveData) ;
                }
            }

            JSONWriter writer = new JSONWriter(response.getWriter());
            Iterator<String> sensitiveDataIt = sensitiveDatas.keySet().iterator();

            writer.object();
            String data = ""; 
            boolean sensitive = false;
            while (sensitiveDataIt.hasNext()) {
                sensitive = true;
               String columnName = sensitiveDataIt.next();
               String cellValue = sensitiveDatas.get(columnName).toString()
                                   .replace("[", "").replace("]", "") ;
               data =  data + "\n\t Column " + columnName+ ":\n\t" + cellValue;
            }
           if(sensitive){
                writer.key("key");
                writer.value("Sensitive data is found in the folloing column/s\n"+data);
           }
           else
           {
               writer.key("key");
               writer.value("There is no sensitive data in this dataset");   
           }
            writer.endObject();

        } catch (Exception e) {
            respondException(response, e);
        } finally {
            ProjectManager.singleton.setBusy(false);
        }
    };

    private  ArrayList checkPrivacy(Column column, Project project, HttpServletRequest request) {

        int cellIndex = column.getCellIndex();

        List values = new ArrayList();// list of okkam IDs to be deleted

        Engine engine = new Engine(project);
        JSONObject engineConfig = null;

        try {
            engineConfig = ParsingUtilities.evaluateJsonStringToObject(request.getParameter("engine"));
        } catch (JSONException e) {
            // ignore
        }

        try {
            engine.initializeFromJSON(engineConfig);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        FilteredRows filteredRows = engine.getAllFilteredRows();
        try {
            filteredRows.accept(project, createRowVisitor(project, cellIndex, values));
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        Iterator<String> valueIt = values.iterator();
       

        int rowIndexes = 0;
        ArrayList sensitiveData = new ArrayList();
        int count = 0;
        while (valueIt.hasNext()) {
            try {
                count++;
                rowIndexes++;
                String value = valueIt.next();
                if (checkEmail(value)||checkPhone(value)||checkSSN(value)) {
                    if(count<=5)//take the first 5 wrong datasets as a sample
                    sensitiveData.add(value);
                    else
                        return sensitiveData;//sure to deduce that the column have sensitive data
                }

            } catch (Exception e) {
                System.out.println("Some error here \n" + e);
            }
        }

        return sensitiveData;//column do not happen to have sensitive data

    }

    private  boolean checkEmail(String email) {
        boolean isEmailIdValid = false;
        if (email != null && email.length() > 0) {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(email);
            if (matcher.matches()) {
                isEmailIdValid = true;
            }
        }
        return isEmailIdValid;
    }

    private  boolean checkSSN(String ssn) {
        boolean isValid = false;
        String expression = "^\\d{3}[- ]\\d{2}[- ]\\d{4}$";
        CharSequence inputStr = ssn;
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }
    private boolean checkPhone(String phone) {
          
        Pattern pattern = Pattern.compile("(([+|-]\\s*(\\d{13}|\\d{10}|\\d{12}))|(^\\d{3}[- ]\\d{3}[- ]\\d{4}$))");
        Matcher matcher = pattern.matcher(phone);
        if (matcher.matches()) {
           return true;
        }
        else
        {
            return false;
        }
        
   }
}
