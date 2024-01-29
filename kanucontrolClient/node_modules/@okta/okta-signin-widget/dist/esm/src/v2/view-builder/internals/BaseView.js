import _Handlebars2 from '../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';
import { View } from '../../../../packages/@okta/courage-dist/esm/src/CourageForSigninWidget.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/handle-url.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-base64.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-i18n.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-img.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-markdown.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/handlebars/helper-xsrfTokenInput.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/vendor/lib/backbone.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';
import BaseHeader from './BaseHeader.js';
import BaseForm from './BaseForm.js';
import BaseModel from './BaseModel.js';
import BaseFooter from './BaseFooter.js';
import { getClassNameMapping } from '../../ion/ViewClassNamesFactory.js';

var BaseView = View.extend({
  Header: BaseHeader,
  Body: BaseForm,
  Footer: BaseFooter,
  className: function () {
    const appState = this.options.appState;
    const formName = appState.get('currentFormName');
    const authenticatorKey = appState.get('authenticatorKey');
    const methodType = appState.get('authenticatorMethodType');
    const isPasswordRecoveryFlow = appState.get('isPasswordRecoveryFlow');
    const additionalClassNames = getClassNameMapping(formName, authenticatorKey, methodType, isPasswordRecoveryFlow);
    const classNames = ['siw-main-view'].concat(additionalClassNames);
    return classNames.join(' ');
  },
  template: _Handlebars2.template({
    "compiler": [8, ">= 4.3.0"],
    "main": function (container, depth0, helpers, partials, data) {
      return "<div class=\"siw-main-header\"></div><div class=\"siw-main-body\"></div><div class=\"siw-main-footer\"></div>";
    },
    "useData": true
  }),
  preRender: function () {
    View.prototype.preRender.apply(this, arguments); // Add Views

    if (this.Header !== null) {
      this.add(this.Header, {
        selector: '.siw-main-header',
        options: this.options
      });
    }

    this.renderForm();
    this.add(this.Footer, {
      selector: '.siw-main-footer',
      options: this.options
    });
  },
  renderForm: function () {
    let optionUiSchemaConfig;

    if (this.form) {
      this.form.remove();
      optionUiSchemaConfig = this.form.model.toJSON({
        verbose: true
      });
    } // Create Model


    const IonModel = this.createModelClass(this.options.currentViewState, optionUiSchemaConfig, this.settings);
    const model = new IonModel({
      formName: this.options.currentViewState.name
    });

    if (!optionUiSchemaConfig) {
      optionUiSchemaConfig = model.toJSON({
        verbose: true
      });
    }

    this.model = model;
    this.form = this.add(this.Body, {
      selector: '.siw-main-body',
      options: Object.assign({}, this.options, {
        model: model,
        optionUiSchemaConfig: optionUiSchemaConfig
      })
    }).last();

    oktaUnderscore.each(model.attributes, (value, key) => {
      if (key.match(/sub_schema_local_[^ ]+/)) {
        // in order to render different sub-schema
        this.listenTo(model, `change:${key}`, () => {
          this.renderForm();
        });
      }
    });
  },
  createModelClass: function (currentViewState, optionUiSchemaConfig = {}) {
    return BaseModel.create(currentViewState, optionUiSchemaConfig);
  }
});

export { BaseView as default };
//# sourceMappingURL=BaseView.js.map
