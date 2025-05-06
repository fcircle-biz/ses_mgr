package com.ses_mgr.admin.service;

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * JUnitテストを直接実行するためのランナー
 * Runner for executing JUnit tests directly
 */
public class TestRunner {

    public static void main(String[] args) {
        // Create a launcher
        Launcher launcher = LauncherFactory.create();
        
        // Create a listener
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        // Register the listener
        launcher.registerTestExecutionListeners(listener);
        
        // Create a discovery request
        DiscoverySelector selector = DiscoverySelectors.selectClass(MasterDataServiceTest.class);
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selector)
                .build();
        
        // Execute tests
        launcher.execute(request);
        
        // Get and print the summary
        TestExecutionSummary summary = listener.getSummary();
        
        // Print the summary
        System.out.println("Tests started: " + summary.getTestsStartedCount());
        System.out.println("Tests succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests failed: " + summary.getTestsFailedCount());
        
        // Print failures if any
        summary.getFailures().forEach(failure -> {
            System.out.println("Test failed: " + failure.getTestIdentifier().getDisplayName());
            System.out.println("Reason: " + failure.getException().getMessage());
            failure.getException().printStackTrace();
        });
    }
}