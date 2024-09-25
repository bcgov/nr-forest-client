import './commands'

Cypress.on('window:before:load', (win) => {
  // Listen to browser console logs and pass them to the Cypress console
  const originalConsoleLog = win.console.log;
  win.console.log = (...args) => {
    originalConsoleLog(...args);
    // Pass logs to Cypress terminal
    Cypress.log({
      name: 'console.log',
      message: [...args],
    });
  };
});