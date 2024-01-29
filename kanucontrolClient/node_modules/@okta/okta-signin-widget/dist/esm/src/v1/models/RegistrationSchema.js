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
import '../../../packages/@okta/courage-dist/esm/src/courage/util/jquery-wrapper.js';
import oktaUnderscore from '../../../packages/@okta/courage-dist/esm/src/courage/util/underscore-wrapper.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/models/Model.js';
import BaseModel from '../../../packages/@okta/courage-dist/esm/src/courage/models/BaseModel.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/framework/View.js';
import '../../../packages/@okta/courage-dist/esm/src/courage/views/Backbone.ListView.js';

/*!
 * Copyright (c) 2017, Okta, Inc. and/or its affiliates. All rights reserved.
 * The Okta software accompanied by this notice is provided pursuant to the Apache License, Version 2.0 (the "License.")
 *
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
let {
  BaseSchema: BaseSchema,
  SchemaProperty: SchemaProperty
} = internal.models;
const RegistrationSchemaRegistrationSchemaPropertyCollection = SchemaProperty.Collection.extend({
  createModelProperties: function () {
    const modelProperties = SchemaProperty.Collection.prototype.createModelProperties.apply(this);

    oktaUnderscore.each(modelProperties, function (modelProperty, name) {
      modelProperty.required = !!this.get(name).get('required');
    }, this);

    return modelProperties;
  }
});
var RegistrationSchema = BaseSchema.Model.extend({
  expand: ['schema'],
  constructor: function () {
    this.properties = new RegistrationSchemaRegistrationSchemaPropertyCollection();
    BaseModel.apply(this, arguments);
  },
  parse: function (resp) {
    const parseResponseData = resp => {
      const requireFields = resp.schema.required;

      if (oktaUnderscore.isArray(requireFields)) {
        oktaUnderscore.each(requireFields, function (requireField) {
          const field = this.properties.get(requireField);

          if (field) {
            field.set('required', true);
          }
        }, this);
      }

      const fieldOrderIds = resp.schema.fieldOrder;

      if (oktaUnderscore.isArray(fieldOrderIds)) {
        oktaUnderscore.each(fieldOrderIds, function (fieldOrderId, sortOrder) {
          const field = this.properties.get(fieldOrderId);

          if (field) {
            field.set('sortOrder', sortOrder);
          }
        }, this);

        this.properties.comparator = 'sortOrder';
        this.properties.sort();
      }

      this.properties.defaultPolicyId = resp.policyId;
      return resp;
    };

    const self = this;
    this.settings.parseRegistrationSchema(resp, function (resp) {
      if (resp.profileSchema) {
        resp.schema = resp.profileSchema;
        BaseSchema.Model.prototype.parse.apply(self, [resp]);
        resp = parseResponseData(resp);
      }

      self.trigger('parseComplete', {
        properties: self.properties
      });
    }, function (error) {
      self.trigger('parseComplete', {
        properties: self.properties,
        error: error
      });
    });
  }
});

export { RegistrationSchema as default };
//# sourceMappingURL=RegistrationSchema.js.map
