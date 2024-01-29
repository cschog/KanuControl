import _Handlebars2 from '../../../../../packages/@okta/courage-dist/esm/lib/handlebars/dist/cjs/handlebars.runtime.js';

const consentLogoHeaderTemplate = _Handlebars2.template({
  "1": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<a href=\"" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "clientURI") || (depth0 != null ? lookupProperty(depth0, "clientURI") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "clientURI",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 26
        },
        "end": {
          "line": 1,
          "column": 39
        }
      }
    }) : helper)) + "\" class=\"client-logo-link\" target=\"_blank\">";
  },
  "3": function (container, depth0, helpers, partials, data) {
    var helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = container.escapeExpression,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<img class=\"client-logo custom-logo\" src=\"" + alias3((helper = (helper = lookupProperty(helpers, "customLogo") || (depth0 != null ? lookupProperty(depth0, "customLogo") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
      "name": "customLogo",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 149
        },
        "end": {
          "line": 1,
          "column": 163
        }
      }
    }) : helper)) + "\" alt=\"" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
      "name": "i18n",
      "hash": {
        "bundle": "login",
        "code": "common.logo.alt"
      },
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 170
        },
        "end": {
          "line": 1,
          "column": 216
        }
      }
    })) + "\" aria-hidden=\"true\" />";
  },
  "5": function (container, depth0, helpers, partials, data) {
    var helper,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        alias2 = container.hooks.helperMissing,
        alias3 = container.escapeExpression,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<img class=\"client-logo default-logo\" src=\"" + alias3((helper = (helper = lookupProperty(helpers, "defaultLogo") || (depth0 != null ? lookupProperty(depth0, "defaultLogo") : depth0)) != null ? helper : alias2, typeof helper === "function" ? helper.call(alias1, {
      "name": "defaultLogo",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 290
        },
        "end": {
          "line": 1,
          "column": 305
        }
      }
    }) : helper)) + "\" alt=\"" + alias3((lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || alias2).call(alias1, {
      "name": "i18n",
      "hash": {
        "bundle": "login",
        "code": "common.logo.alt"
      },
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 312
        },
        "end": {
          "line": 1,
          "column": 358
        }
      }
    })) + "\" aria-hidden=\"true\" />";
  },
  "7": function (container, depth0, helpers, partials, data) {
    return "</a>";
  },
  "9": function (container, depth0, helpers, partials, data) {
    var helper,
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return "<div class=\"issuer\"><span>" + container.escapeExpression((helper = (helper = lookupProperty(helpers, "issuer") || (depth0 != null ? lookupProperty(depth0, "issuer") : depth0)) != null ? helper : container.hooks.helperMissing, typeof helper === "function" ? helper.call(depth0 != null ? depth0 : container.nullContext || {}, {
      "name": "issuer",
      "hash": {},
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 566
        },
        "end": {
          "line": 1,
          "column": 576
        }
      }
    }) : helper)) + "</span></div>";
  },
  "compiler": [8, ">= 4.3.0"],
  "main": function (container, depth0, helpers, partials, data) {
    var stack1,
        alias1 = depth0 != null ? depth0 : container.nullContext || {},
        lookupProperty = container.lookupProperty || function (parent, propertyName) {
      if (Object.prototype.hasOwnProperty.call(parent, propertyName)) {
        return parent[propertyName];
      }

      return undefined;
    };

    return ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "clientURI") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(1, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 0
        },
        "end": {
          "line": 1,
          "column": 89
        }
      }
    })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "customLogo") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(3, data, 0),
      "inverse": container.program(5, data, 0),
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 89
        },
        "end": {
          "line": 1,
          "column": 388
        }
      }
    })) != null ? stack1 : "") + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "clientURI") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(7, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 388
        },
        "end": {
          "line": 1,
          "column": 416
        }
      }
    })) != null ? stack1 : "") + "<h1><span class=\"title-text\">" + ((stack1 = (lookupProperty(helpers, "i18n") || depth0 && lookupProperty(depth0, "i18n") || container.hooks.helperMissing).call(alias1, {
      "name": "i18n",
      "hash": {
        "arguments": "appName",
        "bundle": "login",
        "code": "consent.required.text"
      },
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 445
        },
        "end": {
          "line": 1,
          "column": 519
        }
      }
    })) != null ? stack1 : "") + "</span>" + ((stack1 = lookupProperty(helpers, "if").call(alias1, depth0 != null ? lookupProperty(depth0, "issuer") : depth0, {
      "name": "if",
      "hash": {},
      "fn": container.program(9, data, 0),
      "inverse": container.noop,
      "data": data,
      "loc": {
        "start": {
          "line": 1,
          "column": 526
        },
        "end": {
          "line": 1,
          "column": 596
        }
      }
    })) != null ? stack1 : "") + "</h1>";
  },
  "useData": true
});

export { consentLogoHeaderTemplate as default };
//# sourceMappingURL=consentLogoHeaderTemplate.js.map
