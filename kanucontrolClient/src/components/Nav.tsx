import React from "react";
import { InputText } from "primereact/inputtext";
import { Menubar } from "primereact/menubar";

const Navigation = () => {
  const navlist = [
    {
      label: "Home",
      icon: "pi pi-fw pi-home",
      command: () => {
        window.location.href = "/startmenue";
      },
    },
    {
      label: "Anmelden",
      icon: "pi pi-sign-in",
      command: () => {
        window.location.href = "/login";
      },
    },
  ];

  const start = (
    <img
      src="https://i.ibb.co/wM20B9N/logo-Kanu-Control200px.png"
      alt="KanuControl"
      width="30"></img>
  );
  const end = (
    <InputText
      placeholder="Search"
      type="text"
    />
  );

  return (
    <div>
      <header>
        <nav>
          <ul>
            <Menubar
              model={navlist}
              start={start}
              end={end}
            />
          </ul>
        </nav>
      </header>
    </div>
  );
};

export default React.memo(Navigation);
