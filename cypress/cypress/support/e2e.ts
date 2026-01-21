import './commands'

Cypress.on('window:before:load', (win) => {
  const originalLog = win.console.log.bind(win.console);

  win.console.log = (...args) => {
    originalLog(...args);

    Cypress.log({
      name: 'console.log',
      message: args.map(String),
    });
  };
});
