import _Handlebars2 from '../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View, loc, Form } from '../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import Backbone from '../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import Model from '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import LoginModel from '../models/LoginModel.js';
import RegistrationSchema from '../models/RegistrationSchema.js';
import Q from 'q';
import BaseLoginController from '../util/BaseLoginController.js';
import Enums from '../../util/Enums.js';
import { RegistrationError } from '../../util/Errors.js';
import RegistrationFormFactory from '../util/RegistrationFormFactory.js';
import Util from '../../util/Util.js';
import SubSchema from '../views/registration/SubSchema.js';

const RegistrationControllerFooter = View.extend({
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      var lookupProperty = container.lookupProperty || function (parent, propertyName) {
        if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
          return parent[propertyName];
        }

        return undefined;
      };

      return "<a href=\"#\" class=\"link help\" data-se=\"back-link\">" + container.escapeExpression((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(depth0 != null ? depth0 : container.nullContext || {}, {
        "name": "i18n",
        "hash": {
          "bundle": "login",
          "code": "goback"
        },
        "data": data,
        "loc": {
          "start": {
            "line": 1,
            "column": 50
          },
          "end": {
            "line": 1,
            "column": 87
          }
        }
      })) + "</a>";
    },
    "useData": true
  }),
  className: 'auth-footer',
  events: {
    'click .help': function (e) {
      e.preventDefault();
      this.back();
    }
  },
  back: function () {
    this.state.set('navigateDir', Enums.DIRECTION_BACK);
    this.options.appState.trigger('navigate', '');
  }
});
var RegistrationController = BaseLoginController.extend({
  className: 'registration',
  initialize: function () {
    const RegistrationControllerSchema = RegistrationSchema.extend({
      settings: this.options.settings,
      url: this.options.settings.get('baseUrl') + '/api/v1/registration/form'
    }); // setup schema

    const schema = new RegistrationControllerSchema();
    this.state.set('schema', schema);
  },
  getRegistrationApiUrl: function () {
    const defaultPolicyId = this.settings.get('defaultPolicyId'); // default policyId

    const orgPolicyId = this.options.settings.get('policyId'); // org policyId

    const apiUrl = defaultPolicyId ? this.getRegistrationPolicyApi(defaultPolicyId) : this.getRegistrationPolicyApi(orgPolicyId);
    return apiUrl;
  },
  getRegistrationPolicyApi: function (policyId) {
    return this.options.settings.get('baseUrl') + '/api/v1/registration/' + policyId;
  },
  doPostSubmit: function () {
    if (this.model.get('activationToken')) {
      const self = this; // register via activation token

      self.settings.callGlobalSuccess(Enums.REGISTRATION_COMPLETE, {
        activationToken: this.model.get('activationToken')
      });
      const loginModel = new LoginModel({
        settings: self.model.appState.settings
      });
      loginModel.loginWithActivationToken(this.model.get('activationToken')).then(function (transaction) {
        self.model.trigger('setTransaction', transaction);
      });
    } else {
      // register via activation email
      this.model.appState.set('username', this.model.get('email'));
      this.model.appState.trigger('navigate', 'signin/register-complete');
    }
  },
  registerUser: function (postData) {
    const self = this;
    Object.keys(postData).forEach(k => (oktaUnderscore.isNull(postData[k]) || oktaUnderscore.isUndefined(postData[k]) || postData[k] === '') && delete postData[k]);
    this.model.attributes = postData; // Model.save returns a jqXHR

    Backbone.Model.prototype.save.call(this.model).then(function () {
      self.model.trigger('startSaving');
      const activationToken = self.model.get('activationToken');
      const postSubmitData = activationToken ? activationToken : self.model.get('email');
      self.settings.postRegistrationSubmit(postSubmitData, function () {
        self.doPostSubmit();
      }, function (errors) {
        self.showErrors(errors);
      });
    }).fail(err => {
      const responseJSON = err.responseJSON;

      if (responseJSON && responseJSON.errorCauses.length) {
        const {
          errorCode: errorCode,
          errorCauses: errorCauses
        } = responseJSON;
        const {
          errorSummary: errorSummary,
          reason: reason,
          location: location
        } = errorCauses[0];
        const isNotUniqueValue = errorCode === 'E0000001' && reason === 'UNIQUE_CONSTRAINT';

        if (isNotUniqueValue) {
          this.renderIsNotUniqueError(responseJSON);
        }

        this.renderLegacyLocationErrorIfNeeded(location, errorSummary);
        Util.triggerAfterError(this, new RegistrationError(errorSummary));
      }
    });
  },
  renderIsNotUniqueError: function (error) {
    const {
      location: location
    } = error.errorCauses[0];
    const errorSummary = loc('registration.error.userName.notUniqueWithinOrg', 'login', [location]); // replace generic error message with more specific one
    // without using backbone events because there was a race condition
    // between clearing and triggering errors

    this.$el.find('.okta-form-infobox-error p').text(errorSummary);
  },
  renderLegacyLocationErrorIfNeeded: function (location, errorSummary) {
    // replace generic error message with errorSummary for v1 SIW
    // this makes sure that with legacy location that starts with `data.userProfile`
    // we still see the errorSummary in the error banner instead of only a generic error
    // See example in https://developer.okta.com/docs/reference/registration-hook/#sample-json-payload-of-request
    if (location && /^data\.userProfile.*/.test(location)) {
      this.$el.find('.okta-form-infobox-error p').text(errorSummary);
    }
  },
  createRegistrationModel: function (modelProperties) {
    const self = this;
    const RegistrationControllerModel = Model.extend({
      url: self.getRegistrationApiUrl() + '/register',
      settings: this.settings,
      appState: this.options.appState,
      props: modelProperties,
      local: {
        activationToken: 'string'
      },
      toJSON: function () {
        const data = Model.prototype.toJSON.apply(this, arguments);
        return {
          userProfile: data,
          relayState: this.settings.get('relayState')
        };
      },
      parse: function (resp) {
        this.set('activationToken', resp.activationToken);
        delete resp.activationToken;
        return resp;
      },
      save: function () {
        this.settings.preRegistrationSubmit(this.attributes, function (postData) {
          self.registerUser(postData);
        }, function (errors) {
          self.showErrors(errors);
        });
      }
    });
    return new RegistrationControllerModel();
  },
  showErrors: function (error, hideRegisterButton) {
    //for parseSchema error hide form and show error at form level
    if (error.callback === 'parseRegistrationSchema' && error.errorCauses) {
      error.errorSummary = oktaUnderscore.clone(error.errorCauses[0].errorSummary);
      delete error.errorCauses;
    } //show error on form


    this.model.trigger('error', this.model, {
      responseJSON: error
    }); //throw registration error

    const errMsg = error.callback ? error.callback + ':' + error.errorSummary : error.errorSummary;
    Util.triggerAfterError(this, new RegistrationError(errMsg));

    if (hideRegisterButton) {
      this.$el.find('.button-primary').hide();
    }
  },
  fetchInitialData: function () {
    const self = this; // register parse complete event listener

    self.state.get('schema').on('parseComplete', function (updatedSchema) {
      const modelProperties = updatedSchema.properties.createModelProperties();
      self.settings.set('defaultPolicyId', updatedSchema.properties.defaultPolicyId); // create model

      self.model = self.createRegistrationModel(modelProperties); // create form

      const RegistrationControllerForm = Form.extend({
        layout: 'o-form-theme',
        autoSave: true,
        noCancelButton: true,
        title: loc('registration.form.title', 'login'),
        save: loc('registration.form.submit', 'login'),
        modelEvents: {
          'invalid': 'modifyErrors'
        },
        hasSavingState: true,
        customSavingState: {
          start: 'startSaving',
          stop: 'stopSaving'
        },
        modifyErrors: function (model, errorResp) {
          // overwrite courage errorResp object to show custom error message
          for (let formFieldName in errorResp) {
            if (errorResp[formFieldName] === 'model.validation.field.string.minLength') {
              errorResp[formFieldName] = loc('registration.model.validation.field.string.too.short', 'login', [model.props[formFieldName].minLength]);
            } else if (errorResp[formFieldName] === 'model.validation.field.string.maxLength') {
              errorResp[formFieldName] = loc('registration.model.validation.field.string.too.long', 'login', [model.props[formFieldName].maxLength + 1]);
            }
          }
        }
      });
      const form = new RegistrationControllerForm(self.toJSON()); // add form

      self.add(form); // add footer

      self.footer = new self.Footer(self.toJSON());
      self.add(self.footer);
      self.addListeners();

      if (updatedSchema.error) {
        self.showErrors(updatedSchema.error, true);
      } else {
        // add fields
        updatedSchema.properties.each(function (schemaProperty) {
          const inputOptions = RegistrationFormFactory.createInputOptions(schemaProperty);
          const subSchemas = schemaProperty.get('subSchemas');
          const name = schemaProperty.get('name');
          form.addInput(inputOptions);

          if (name === 'password' && subSchemas) {
            form.add(SubSchema.extend({
              id: 'subschemas-' + name,
              subSchemas: subSchemas
            }));
          }
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

        form.add(requiredFieldsLabel);
      }
    }); // fetch schema from API, returns a jqXHR. Wrap it in a Q

    return Q(this.state.get('schema').fetch());
  },
  Footer: RegistrationControllerFooter
});

export { RegistrationController as default };
//# sourceMappingURL=RegistrationController.js.map
