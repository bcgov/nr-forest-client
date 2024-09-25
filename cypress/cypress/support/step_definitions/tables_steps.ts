import { Then, DataTable, Step, BeforeStep } from "@badeball/cypress-cucumber-preprocessor";

let idir = true;

BeforeStep({ tags: "@loginAsBCeID or @loginAsBCSC" }, function () {
  idir = false;
});
BeforeStep({ tags: "@loginAsEditor or @loginAsViewer or @loginAsAdmin" }, function () {
  idir = true;
});

/* Data tables */

Then('I fill the form as follows', (table: DataTable) => {
  table.rows().forEach(row => {
    const [fieldName, value, kind] = row;
    if (kind === 'text') {
      Step(this, `I type "${value}" into the "${fieldName.trim()}" form input`);
    }else if (kind === 'textreplace') {
      Step(this, `I replace the "${fieldName.trim()}" with "${value}" form input`);
    } else if (kind === 'select') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" form input`);
    } else if (kind === 'autocomplete') {
      Step(this, `I type "${value}" and select "${value}" from the "${fieldName.trim()}" form autocomplete`);
    } else if (kind === 'multiselect') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" multiselect`);
    }
  });
});

Then('I fill the {string} address with the following', (location: string, table: DataTable) => {
  table.rows().forEach(row => {
    const [fieldName, value, kind] = row;
    if (kind === 'text') {
      Step(this, `I type "${value}" into the "${fieldName.trim()}" form input for the "${location}"`);
    } else if (kind === 'textreplace') {
      Step(this, `I replace the "${fieldName.trim()}" with "${value}" form input for the "${location}"`);
    } else if (kind === 'select') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" form input for the "${location}"`);
    } else if (kind === 'autocomplete') {
      Step(this, `I type "${value}" and select "${value}" from the "${fieldName.trim()}" form autocomplete for the "${location}"`);
    } else if (kind === 'multiselect') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" multiselect`);
    }
  });
});

Then('I fill the {string} information with the following', (contactName: string, table: DataTable) => {
  table.rows().forEach(row => {
    const [fieldName, value, kind] = row;
    if (kind === 'text') {
      Step(this, `I type "${value}" into the "${fieldName.trim()}" form input for the "${contactName.trim()}"`);
    } else if (kind === 'textreplace') {
      Step(this, `I replace the "${fieldName.trim()}" with "${value}" form input for the "${contactName}"`);
    } else if (kind === 'select') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" form input for the "${contactName.trim()}"`);
    } else if (kind === 'autocomplete') {
      Step(this, `I type "${value}" and select "${value}" from the "${fieldName.trim()}" form autocomplete for the "${contactName.trim()}"`);
    } else if (kind === 'multiselect') {
      Step(this, `I select "${value}" from the "${fieldName.trim()}" multiselect for the "${contactName.trim()}"`);
    }
  });
});