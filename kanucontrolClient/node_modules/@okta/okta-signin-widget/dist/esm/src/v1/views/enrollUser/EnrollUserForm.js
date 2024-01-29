import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { Form, loc } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import ProfileSchema from '../../models/ProfileSchema.js';
import RegistrationFormFactory from '../../util/RegistrationFormFactory.js';

var EnrollUserForm = Form.extend({
  layout: 'o-form-theme',
  autoSave: true,
  noCancelButton: true,
  title: function () {
    return loc('registration.form.title', 'login');
  },
  save: function () {
    return loc('registration.form.submit', 'login');
  },
  initialize: function (options) {
    this.options = options || {};
    this.schema = new ProfileSchema({
      profileSchemaAttributes: this.options.appState.get('policy').registration.profile
    });
    this.schema.properties.each(schemaProperty => {
      const inputOptions = RegistrationFormFactory.createInputOptions(schemaProperty);
      this.addInput(inputOptions);
    });

    const requiredFieldsLabel = _Handlebars2.template({
      "compiler": [8, ">= 4.3.0"],
      "main": function (container, depth0, helpers, partials, data) {
        var helper,
            lookupProperty = container.lookupProperty || function (parent, propertyName) {
          if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
            return parent[propertyName];
          }

          return undefined;
        };

        return "<span class=\"required-fields-label\">" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "label") || (depth0 != null ? lookupProperty(depth0, "label") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
          "name": "label",
          "hash": {},
          "data": data,
          "loc": {
            "start": {
              "line": 1,
              "column": 36
            },
            "end": {
              "line": 1,
              "column": 45
            }
          }
        }) : helper)) + "</span>";
      },
      "useData": true
    })({
      label: loc('registration.required.fields.label', 'login')
    });

    this.add(requiredFieldsLabel);
  }
});

export { EnrollUserForm as default };
//# sourceMappingURL=EnrollUserForm.js.map
