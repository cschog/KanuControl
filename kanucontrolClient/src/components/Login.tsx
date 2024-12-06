import React, { useState } from "react";
import { InputText } from "primereact/inputtext";
import { Checkbox } from "primereact/checkbox";
import { Button } from "primereact/button";

const Login = () => {
  const [checked, setChecked] = useState(false); // Initialize with a boolean value

  function callStartMenue() {
    window.location.href = "/startmenue";
  }

  return (
    <div className="flex align-items-center justify-content-center">
      <div className="surface-card p-4 shadow-2 border-round w-full lg:w-6">
        <div className="text-center">
          <img
            src="https://i.ibb.co/wM20B9N/logo-Kanu-Control200px.png"
            alt="KanuControl"
            width="60"
          />
          <div className="text-900 text-3xl font-medium mb-3">KanuControl</div>
          <span className="text-600 font-medium line-height-3">
            Noch nicht registriert?
          </span>
          <a
            href="##"
            className="font-medium no-underline ml-2 text-blue-500 cursor-pointer">
            Hier registrieren!
          </a>
        </div>

        <div>
          <label
            htmlFor="Email"
            className="block text-900 font-medium mb-2">
            Email
          </label>
          <InputText
            id="email"
            type="text"
            className="w-full mb-3"
          />

          <label
            htmlFor="password"
            className="block text-900 font-medium mb-2">
            Passwort
          </label>
          <InputText
            id="password"
            type="password"
            className="w-full mb-3"
          />

          <div className="flex align-items-center justify-content-between mb-6">
            <div className="flex align-items-center">
              <Checkbox
                id="rememberme"
                onChange={(e) => setChecked(e.checked)}
                checked={checked}
                className="mr-2"
              />
              <label htmlFor="rememberme">Anmeldung speichern?</label>
            </div>
            <a
              href="##"
              className="font-medium no-underline ml-2 text-blue-500 text-right cursor-pointer">
              Passwort vergessen?
            </a>
          </div>

          <Button
            label="Anmelden"
            icon="pi pi-user"
            className="w-full bg-blue-700"
            onClick={callStartMenue}></Button>
        </div>
      </div>
    </div>
  );
};

export default Login;
