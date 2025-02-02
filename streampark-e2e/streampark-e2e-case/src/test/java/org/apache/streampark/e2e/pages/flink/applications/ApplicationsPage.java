/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.streampark.e2e.pages.flink.applications;

import org.apache.streampark.e2e.pages.common.Constants;
import org.apache.streampark.e2e.pages.common.NavBarPage;
import org.apache.streampark.e2e.pages.flink.ApacheFlinkPage;

import lombok.Getter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.apache.streampark.e2e.pages.common.CommonFactory.WebDriverWaitForElementVisibilityAndInvisibility;

@Getter
public class ApplicationsPage extends NavBarPage implements ApacheFlinkPage.Tab {

    @FindBy(className = "e2e-flinkapp-create-btn")
    private WebElement buttonCreateApplication;

    @FindBy(className = "ant-table-tbody")
    private List<WebElement> applicationsList;

    @FindBy(className = "ant-form-item-explain-error")
    private List<WebElement> errorMessageList;

    @FindBy(xpath = "//div[contains(@class, 'ant-dropdown-content')]//span[contains(text(), 'Delete')]")
    private WebElement deleteButton;

    @FindBy(xpath = "//button[contains(@class, 'ant-btn')]/span[contains(., 'OK')]")
    private WebElement deleteConfirmButton;

    public ApplicationsPage(RemoteWebDriver driver) {
        super(driver);
    }

    public ApplicationForm createApplication() {
        waitForPageLoading();

        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(ExpectedConditions.elementToBeClickable(buttonCreateApplication));

        buttonCreateApplication.click();
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(ExpectedConditions.urlContains("/flink/app/add"));
        return new ApplicationForm(driver);
    }

    public ApplicationsPage deleteApplication(String applicationName) {
        waitForPageLoading();

        WebElement extraButton = applicationsList().stream()
            .filter(it -> it.getText().contains(applicationName))
            .flatMap(
                it -> it.findElements(By.xpath("//span[contains(@aria-label, 'more')]/.."))
                    .stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No extra button in applications list"));
        Actions actions = new Actions(this.driver);
        actions.moveToElement(extraButton).perform();
        deleteButton.click();
        deleteConfirmButton.click();

        return this;
    }

    public ApplicationsPage startApplication(String applicationName) {
        waitForPageLoading();

        applicationsList().stream()
            .filter(it -> it.getText().contains(applicationName))
            .flatMap(it -> it.findElements(By.xpath("//button[contains(@auth, 'app:start')]")).stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No start button in applications list"))
            .click();

        StartJobForm startJobForm = new StartJobForm();
        String startJobFormMessage = "Start Job";
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(String.format("//*[contains(.,'%s')]",
                        startJobFormMessage))));
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(ExpectedConditions.elementToBeClickable(startJobForm.radioFromSavepoint()));
        startJobForm.radioFromSavepoint().click();
        startJobForm.buttonSubmit().click();
        String startPopUpMessage = "The current job is starting";
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(String.format("//*[contains(text(),'%s')]",
                        startPopUpMessage))));
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(
                ExpectedConditions.invisibilityOfElementLocated(
                    By.xpath(String.format("//*[contains(text(),'%s')]",
                        startPopUpMessage))));

        return this;
    }

    public ApplicationsPage releaseApplication(String applicationName) {
        waitForPageLoading();

        applicationsList().stream()
            .filter(it -> it.getText().contains(applicationName))
            .flatMap(
                it -> it.findElements(By.xpath("//button[contains(@auth, 'app:release')]"))
                    .stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No release button in applications list"))
            .click();

        return this;
    }

    public ApplicationsPage cancelApplication(String applicationName) {
        waitForPageLoading();

        applicationsList().stream()
            .filter(it -> it.getText().contains(applicationName))
            .flatMap(
                it -> it.findElements(By.xpath("//button[contains(@auth, 'app:cancel')]"))
                    .stream())
            .filter(WebElement::isDisplayed)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No cancel button in applications list"))
            .click();

        CancelJobForm cancelJobForm = new CancelJobForm();
        String cancelJobFormMessage = "Stop Job";
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.xpath(String.format("//*[contains(.,'%s')]",
                        cancelJobFormMessage))));
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(ExpectedConditions.elementToBeClickable(cancelJobForm.radioFromSavepoint()));
        cancelJobForm.radioFromSavepoint().click();
        cancelJobForm.buttonSubmit().click();
        String cancelPopUpMessage = "The current job is canceling";
        WebDriverWaitForElementVisibilityAndInvisibility(driver, cancelPopUpMessage);

        return this;
    }

    private void waitForPageLoading() {
        new WebDriverWait(driver, Constants.DEFAULT_WEBDRIVER_WAIT_DURATION)
            .until(ExpectedConditions.urlContains("/flink/app"));
    }

    @Getter
    public class StartJobForm {

        StartJobForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(xpath = "//button[@id='startApplicationModal_restoreSavepoint']//span[contains(text(), 'ON')]")
        private WebElement radioFromSavepoint;

        @FindBy(xpath = "//div[contains(.,'Start Job')]//button[contains(@class, 'ant-btn')]//span[contains(., 'Apply')]")
        private WebElement buttonSubmit;

        @FindBy(xpath = "//button[contains(@class, 'ant-btn')]//span[contains(., 'Cancel')]")
        private WebElement buttonCancel;
    }

    @Getter
    public class CancelJobForm {

        CancelJobForm() {
            PageFactory.initElements(driver, this);
        }

        @FindBy(xpath = "//span[contains(text(), 'ON')]")
        private WebElement radioFromSavepoint;

        @FindBy(xpath = "//div[contains(.,'Stop Job')]//button[contains(@class, 'ant-btn')]//span[contains(., 'Apply')]")
        private WebElement buttonSubmit;

        @FindBy(xpath = "//button[contains(@class, 'ant-btn')]//span[contains(., 'Cancel')]")
        private WebElement buttonCancel;
    }
}
