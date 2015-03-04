package edu.asu.bscs.csiebler.calculatorrpc;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Copyright 2015 Cory Siebler
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * @author Cory Siebler csiebler@asu.edu
 * @version Feb 03, 2015
 */
public class MainActivity extends ActionBarActivity {

    // Numeric Regular Expression
    private static final String NUMBER_REGEX = "-?\\d+(\\.\\d+)?";

    private CalcJavaClient client;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load the Enum values to the Spinner
        Spinner operationSpinner = (Spinner) findViewById(R.id.operation_spinner);
        operationSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, Operation.values()));

        // Initialize the Calculator Client
        client = new CalcJavaClient(this.getString(R.string.url_string));
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void submitButton(View v) {
        // Retrieve the Input Fields
        EditText leftInput = (EditText) findViewById(R.id.left_input);
        EditText rightInput = (EditText) findViewById(R.id.right_input);

        // Retrieve the Answer Label
        TextView answerLabel = (TextView) findViewById(R.id.answer_label);

        // Retrieve the Spinner Input
        Spinner operationSpinner = (Spinner) findViewById(R.id.operation_spinner);

        // Make sure the user input is valid
        if (leftInput.getText().toString().isEmpty()
                || rightInput.getText().toString().isEmpty()) {
            // Test for input
            answerLabel.setText("ERROR - No input given");
            return;
        } else if (!(leftInput.getText().toString().matches(NUMBER_REGEX)
                && rightInput.getText().toString().matches(NUMBER_REGEX))) {
            // Test for numeric input
            answerLabel.setText("ERROR - Invalid input given");
            return;
        }

        // Grab the user input
        final double leftValue = Double.parseDouble(leftInput.getText().toString());
        final double rightValue = Double.parseDouble(rightInput.getText().toString());
        final Operation operation = (Operation) operationSpinner.getSelectedItem();

        // Make sure mathematical errors do not exist
        if (operation.equals(Operation.DIVIDE) && rightValue == 0) {
            // Test for divide by zero
            answerLabel.setText("ERROR - Divide by zero");
            return;
        }

        // Initialize a new session to the Calculator RPC Call
        CalculatorRPCCall session = new CalculatorRPCCall(operation, leftValue, rightValue);

        session.start();

        while (!session.getState().equals(Thread.State.TERMINATED)) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        answerLabel.setText(String.valueOf(session.getResult()));

        Log.w(getClass().getName(), "RESULT: " + session.getResult());
    }

    /**
     *
     */
    private class CalculatorRPCCall extends Thread {

        private double result;
        private double leftValue;
        private double rightValue;
        private Operation operation;

        /**
         * Constructor
         * @param operation
         * @param leftValue
         * @param rightValue
         */
        public CalculatorRPCCall(Operation operation, double leftValue, double rightValue) {
            this.operation = operation;
            this.leftValue = leftValue;
            this.rightValue = rightValue;
        }

        /**
         *
         */
        @Override
        public void run() {
            switch (operation) {
                case ADD:
                    result = client.add(leftValue, rightValue);
                    Log.w(getClass().getSimpleName(), "CALLED: ADD");
                    break;
                case DIVIDE:
                    result = client.divide(leftValue, rightValue);
                    Log.w(getClass().getSimpleName(), "CALLED: DIVIDE");
                    break;
                case MULTIPLY:
                    result = client.multiply(leftValue, rightValue);
                    Log.w(getClass().getSimpleName(), "CALLED: MULTIPLY");
                    break;
                case SUBTRACT:
                    result = client.subtract(leftValue, rightValue);
                    Log.w(getClass().getSimpleName(), "CALLED: SUBTRACT");
                    break;
                default:
                    result = 0;
                    return;
            }

            Log.w(getClass().getSimpleName(), "RESULT: " + result);
        }

        /**
         *
         * @return
         */
        public double getResult() {
            return result;
        }
    }
}
