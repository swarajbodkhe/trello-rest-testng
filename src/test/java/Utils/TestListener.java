package Utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static ExtentReports extent = ExtentManager.createInstance();
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    @Override
    public synchronized void onTestStart(ITestResult iTestResult) {
        System.out.println((iTestResult.getMethod().getMethodName() + " started!"));
        ExtentTest extentTest = extent.createTest(iTestResult.getMethod().getMethodName(),iTestResult.getMethod().getDescription());
        test.set(extentTest);
    }

    @Override
    public synchronized void onTestSuccess(ITestResult iTestResult) {
        System.out.println(iTestResult.getMethod().getMethodName()+" passed!");
        test.get().pass("Test Passed");
    }

    @Override
    public synchronized void onTestFailure(ITestResult iTestResult) {
        System.out.println(iTestResult.getMethod().getMethodName()+" failed!");
        test.get().fail(iTestResult.getThrowable());
    }

    @Override
    public synchronized void onTestSkipped(ITestResult iTestResult) {
        System.out.println(iTestResult.getMethod().getMethodName()+" skipped!");
        test.get().fail(iTestResult.getThrowable());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        System.out.println(("onTestFailedButWithinSuccessPercentage for " + iTestResult.getMethod().getMethodName()));
    }

    @Override
    public synchronized void onStart(ITestContext iTestContext) {
        System.out.println("Extent Reports Version 3 Test Suite started!");
    }

    @Override
    public synchronized void onFinish(ITestContext iTestContext) {
        System.out.println(("Extent Reports Version 3  Test Suite is ending!"));
        extent.flush();
    }
}
