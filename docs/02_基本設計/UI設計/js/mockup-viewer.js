/**
 * SES業務システム - Mockup Viewer
 * 
 * This script enables the display of HTML mockups within wireframe documentation
 * via iframes with responsive sizing and navigation controls.
 */

class MockupViewer {
    constructor(containerId, options = {}) {
        this.container = document.getElementById(containerId);
        if (!this.container) {
            console.error(`MockupViewer: Container element with ID "${containerId}" not found.`);
            return;
        }

        this.options = {
            defaultHeight: options.defaultHeight || '600px',
            responsive: options.responsive !== false,
            showControls: options.showControls !== false,
            showBorder: options.showBorder !== false,
            deviceModes: options.deviceModes || ['desktop', 'tablet', 'mobile'],
            mockupPath: options.mockupPath || '',
            defaultActive: options.defaultActive || 0,
            mockups: options.mockups || []
        };

        this.currentIndex = this.options.defaultActive;
        this.currentDevice = 'desktop';
        
        this.init();
    }

    init() {
        this.createContainer();
        this.createControls();
        this.createIframe();
        this.loadMockup(this.currentIndex);
        this.bindEvents();
    }

    createContainer() {
        this.container.classList.add('mockup-viewer-container');
        
        const containerStyle = document.createElement('style');
        containerStyle.textContent = `
            .mockup-viewer-container {
                margin: 20px 0;
                background-color: #f8f9fa;
                border-radius: 8px;
                overflow: hidden;
                ${this.options.showBorder ? 'border: 1px solid #dee2e6;' : ''}
            }
            .mockup-viewer-controls {
                display: flex;
                align-items: center;
                justify-content: space-between;
                padding: 12px 15px;
                background-color: #f8f9fa;
                border-bottom: 1px solid #dee2e6;
            }
            .mockup-tabs {
                display: flex;
                gap: 10px;
            }
            .mockup-tab {
                padding: 6px 12px;
                border-radius: 4px;
                cursor: pointer;
                background-color: #e9ecef;
                border: none;
                font-size: 14px;
                transition: all 0.2s;
            }
            .mockup-tab.active {
                background-color: #0d6efd;
                color: white;
            }
            .device-controls {
                display: flex;
                gap: 10px;
                align-items: center;
            }
            .device-button {
                padding: 6px;
                background: none;
                border: none;
                border-radius: 4px;
                cursor: pointer;
                opacity: 0.5;
                transition: all 0.2s;
            }
            .device-button.active {
                opacity: 1;
                background-color: #e9ecef;
            }
            .mockup-iframe-container {
                width: 100%;
                transition: width 0.3s ease;
                margin: 0 auto;
                overflow: hidden;
            }
            .mockup-iframe {
                width: 100%;
                border: none;
                display: block;
                background-color: #fff;
            }
            .device-desktop .mockup-iframe-container {
                width: 100%;
            }
            .device-tablet .mockup-iframe-container {
                width: 768px;
                max-width: 100%;
            }
            .device-mobile .mockup-iframe-container {
                width: 375px;
                max-width: 100%;
            }
        `;
        document.head.appendChild(containerStyle);
    }

    createControls() {
        if (!this.options.showControls) return;
        
        const controlsDiv = document.createElement('div');
        controlsDiv.className = 'mockup-viewer-controls';
        
        // Create tabs for mockup selection
        if (this.options.mockups.length > 1) {
            const tabsDiv = document.createElement('div');
            tabsDiv.className = 'mockup-tabs';
            
            this.options.mockups.forEach((mockup, index) => {
                const tabButton = document.createElement('button');
                tabButton.className = `mockup-tab ${index === this.currentIndex ? 'active' : ''}`;
                tabButton.textContent = mockup.name || `モックアップ ${index + 1}`;
                tabButton.dataset.index = index;
                tabButton.addEventListener('click', () => this.loadMockup(index));
                tabsDiv.appendChild(tabButton);
            });
            
            controlsDiv.appendChild(tabsDiv);
        }
        
        // Create device mode controls
        if (this.options.responsive) {
            const deviceControlsDiv = document.createElement('div');
            deviceControlsDiv.className = 'device-controls';
            
            // Desktop icon
            const desktopButton = document.createElement('button');
            desktopButton.className = 'device-button active';
            desktopButton.dataset.device = 'desktop';
            desktopButton.innerHTML = '<svg width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M0 4s0-2 2-2h12s2 0 2 2v6s0 2-2 2h-4c0 .667.083 1.167.25 1.5H11a.5.5 0 0 1 0 1H5a.5.5 0 0 1 0-1h.75c.167-.333.25-.833.25-1.5H2s-2 0-2-2V4zm1.398-.855a.758.758 0 0 0-.254.302A1.46 1.46 0 0 0 1 4.01V10c0 .325.078.502.145.602.07.105.17.188.302.254a1.464 1.464 0 0 0 .538.143L2.01 11H14c.325 0 .502-.078.602-.145a.758.758 0 0 0 .254-.302 1.464 1.464 0 0 0 .143-.538L15 9.99V4c0-.325-.078-.502-.145-.602a.757.757 0 0 0-.302-.254A1.46 1.46 0 0 0 13.99 3H2c-.325 0-.502.078-.602.145z"/></svg>';
            desktopButton.title = 'デスクトップ表示';
            
            // Tablet icon
            const tabletButton = document.createElement('button');
            tabletButton.className = 'device-button';
            tabletButton.dataset.device = 'tablet';
            tabletButton.innerHTML = '<svg width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M12 1a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h8zM4 0a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2H4z"/><path d="M8 14a1 1 0 1 0 0-2 1 1 0 0 0 0 2z"/></svg>';
            tabletButton.title = 'タブレット表示';
            
            // Mobile icon
            const mobileButton = document.createElement('button');
            mobileButton.className = 'device-button';
            mobileButton.dataset.device = 'mobile';
            mobileButton.innerHTML = '<svg width="20" height="20" fill="currentColor" viewBox="0 0 16 16"><path d="M11 1a1 1 0 0 1 1 1v12a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h6zM5 0a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h6a2 2 0 0 0 2-2V2a2 2 0 0 0-2-2H5z"/><path d="M8 14a1 1 0 1 0 0-2 1 1 0 0 0 0 2z"/></svg>';
            mobileButton.title = 'モバイル表示';
            
            deviceControlsDiv.appendChild(desktopButton);
            deviceControlsDiv.appendChild(tabletButton);
            deviceControlsDiv.appendChild(mobileButton);
            
            controlsDiv.appendChild(deviceControlsDiv);
        }
        
        this.container.appendChild(controlsDiv);
        this.controlsDiv = controlsDiv;
    }

    createIframe() {
        const iframeContainer = document.createElement('div');
        iframeContainer.className = 'mockup-iframe-container';
        
        const iframe = document.createElement('iframe');
        iframe.className = 'mockup-iframe';
        iframe.style.height = this.options.defaultHeight;
        iframe.setAttribute('allowfullscreen', 'true');
        iframe.setAttribute('loading', 'lazy');
        
        iframeContainer.appendChild(iframe);
        this.container.appendChild(iframeContainer);
        
        this.iframe = iframe;
        this.iframeContainer = iframeContainer;
    }

    loadMockup(index) {
        if (index < 0 || index >= this.options.mockups.length) {
            console.error(`MockupViewer: Invalid mockup index: ${index}`);
            return;
        }
        
        const mockup = this.options.mockups[index];
        const mockupPath = mockup.path || '';
        
        if (!mockupPath) {
            console.error(`MockupViewer: Mockup path not specified for index ${index}`);
            return;
        }
        
        // Update iframe src
        const fullPath = this.options.mockupPath ? 
            `${this.options.mockupPath}/${mockupPath}` : mockupPath;
        
        this.iframe.src = fullPath;
        this.currentIndex = index;
        
        // Update tabs if they exist
        if (this.controlsDiv) {
            const tabs = this.controlsDiv.querySelectorAll('.mockup-tab');
            tabs.forEach((tab, i) => {
                if (i === index) {
                    tab.classList.add('active');
                } else {
                    tab.classList.remove('active');
                }
            });
        }
    }

    setDeviceMode(device) {
        if (!this.options.deviceModes.includes(device)) {
            console.error(`MockupViewer: Invalid device mode: ${device}`);
            return;
        }
        
        this.container.className = 'mockup-viewer-container device-' + device;
        this.currentDevice = device;
        
        // Update device buttons if they exist
        if (this.controlsDiv) {
            const deviceButtons = this.controlsDiv.querySelectorAll('.device-button');
            deviceButtons.forEach(button => {
                if (button.dataset.device === device) {
                    button.classList.add('active');
                } else {
                    button.classList.remove('active');
                }
            });
        }
    }

    bindEvents() {
        if (!this.controlsDiv) return;
        
        // Device mode buttons
        const deviceButtons = this.controlsDiv.querySelectorAll('.device-button');
        deviceButtons.forEach(button => {
            button.addEventListener('click', () => {
                this.setDeviceMode(button.dataset.device);
            });
        });
        
        // Iframe load event for auto-height
        this.iframe.addEventListener('load', () => {
            try {
                // Try to adjust iframe height to content height
                const iframeDoc = this.iframe.contentDocument || this.iframe.contentWindow.document;
                if (iframeDoc) {
                    const height = iframeDoc.body.scrollHeight;
                    if (height > 100) {
                        this.iframe.style.height = height + 'px';
                    }
                }
            } catch (e) {
                // Cross-origin issues might prevent accessing iframe content
                console.warn('MockupViewer: Could not adjust iframe height - cross-origin restriction');
            }
        });
    }
}

// Make it globally available
window.MockupViewer = MockupViewer;