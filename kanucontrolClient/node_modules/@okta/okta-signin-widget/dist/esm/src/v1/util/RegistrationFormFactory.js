import { internal } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import oktaJQueryStatic from '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import TextBox from '../views/shared/TextBox.js';

/* eslint max-statements: [2, 23],  max-depth: [2, 3], complexity: [2, 13] */
let {
  SchemaFormFactory: SchemaFormFactory
} = internal.views.forms.helpers;

const getParts = function (username) {
  const usernameArr = username.split('');
  const minPartsLength = 3;
  const userNameParts = [];
  const delimiters = [',', '.', '-', '_', '#', '@'];
  let userNamePart = '';

  oktaUnderscore.each(usernameArr, function (part) {
    if (delimiters.indexOf(part) === -1) {
      userNamePart += part;
    } else {
      if (userNamePart.length >= minPartsLength) {
        userNameParts.push(oktaUnderscore.clone(userNamePart));
      }

      userNamePart = '';
    }
  });

  if (userNamePart.length >= minPartsLength) {
    userNameParts.push(oktaUnderscore.clone(userNamePart));
  }

  return userNameParts.filter(Boolean);
};

const passwordContainsFormField = function (formField, password) {
  if (!formField) {
    return false;
  }

  formField = formField.toLowerCase();
  password = password.toLowerCase();
  const formFieldArr = getParts(formField); //check if each formField part contains password

  for (var i = 0; i < formFieldArr.length; i++) {
    const formFieldPart = formFieldArr[i];

    if (password.indexOf(formFieldPart) !== -1) {
      return true;
    }
  }

  return false;
};

const checkSubSchema = function (subSchema, value, model) {
  const minLength = subSchema.get('minLength');
  const maxLength = subSchema.get('maxLength');
  const regex = subSchema.get('format');

  if (oktaUnderscore.isNumber(minLength)) {
    if (value.length < minLength) {
      return false;
    }
  }

  if (oktaUnderscore.isNumber(maxLength)) {
    if (value.length > maxLength) {
      return false;
    }
  }

  const password = value;

  if (oktaUnderscore.isString(regex)) {
    // call passwordContainsFormField if regex is userName, firstName, lastName
    if (regex === '^[#/userName]' || regex === '^[#/firstName]' || regex === '^[#/lastName]') {
      const fieldName = regex.split('^[#/')[1].split(']')[0];
      let fieldValue = model.get(fieldName);

      if (fieldName === 'userName') {
        // with email as login enabled, we only have email populated
        // Therefore we fallback and run validation with email attribute.
        fieldValue = model.has('userName') ? model.get('userName') : model.get('email');
      }

      return !passwordContainsFormField(fieldValue, password);
    } else {
      if (!new RegExp(regex).test(value)) {
        return false;
      }
    }
  }

  return true;
};

const checkSubSchemas = function (fieldName, model, subSchemas, showError) {
  const value = model.get(fieldName);

  if (!oktaUnderscore.isString(value)) {
    return;
  }

  subSchemas.each(function (subSchema, index) {
    const ele = oktaJQueryStatic('#subschemas-' + fieldName + ' .subschema-' + index); //hide password complexity if no password

    if (value) {
      ele.children('p').removeClass('default-schema');
    } else {
      ele.children('p').addClass('default-schema');
    } // clear aria role and live-region for re-validation


    ele.children('p').removeAttr('role').removeAttr('aria-live'); // reset errors

    ele.removeClass('subschema-satisfied subschema-unsatisfied subschema-error'); // validate

    if (checkSubSchema(subSchema, value, model)) {
      // passed
      ele.addClass('subschema-satisfied');
      ele.find('p span').removeClass('error error-16-small');
      ele.find('p span').addClass('confirm-16');
    } else {
      // failed
      if (showError) {
        ele.find('p span').removeClass('confirm-16');
        ele.find('p span').addClass('error error-16-small');
        ele.addClass('subschema-error subschema-unsatisfied');
        ele.find('p') // set role="alert" so the password requirement is read by
        // screen-readers
        .attr('role', 'alert') // set aria-live="polite" so it will "debounce" and wait to read the
        // message between keystrokes
        .attr('aria-live', 'polite');
      }
    }
  });
};

const fnCreateInputOptions = function (schemaProperty) {
  let inputOptions = SchemaFormFactory.createInputOptions(schemaProperty);

  if (inputOptions.type === 'select') {
    inputOptions = oktaUnderscore.extend(inputOptions, {
      label: schemaProperty.get('title')
    });
  } else {
    let placeholder = schemaProperty.get('title');

    if (schemaProperty.get('required')) {
      placeholder += ' *';
    }

    inputOptions = oktaUnderscore.extend(inputOptions, {
      label: false,
      'label-top': true,
      placeholder: placeholder
    });
  }

  const fieldName = schemaProperty.get('name');

  switch (fieldName) {
    case 'userName':
      inputOptions.input = TextBox;
      inputOptions.params = {
        icon: 'person-16-gray'
      };
      break;

    case 'password':
      inputOptions.type = 'password';
      inputOptions.input = TextBox;
      inputOptions.params = {
        icon: 'remote-lock-16'
      };
  }

  const subSchemas = schemaProperty.get('subSchemas');

  if (subSchemas) {
    inputOptions.events = {
      input: function () {
        checkSubSchemas(fieldName, this.model, subSchemas, true);
      },
      focusout: function () {
        checkSubSchemas(fieldName, this.model, subSchemas, true);
      },
      'change:userName': function () {
        checkSubSchemas(fieldName, this.model, subSchemas, true);
      },
      'change:firstName': function () {
        checkSubSchemas(fieldName, this.model, subSchemas, true);
      },
      'change:lastName': function () {
        checkSubSchemas(fieldName, this.model, subSchemas, true);
      },
      'change:email': function () {
        checkSubSchemas(fieldName, this.model, subSchemas, true);
      }
    };
  }

  return inputOptions;
};

var RegistrationFormFactory = {
  createInputOptions: fnCreateInputOptions,
  getUsernameParts: getParts,
  passwordContainsFormField: passwordContainsFormField
};

export { RegistrationFormFactory as default };
//# sourceMappingURL=RegistrationFormFactory.js.map
