// src/components/veranstaltung/Veranstaltungen.tsx
import { Component } from "react";
import { MenueHeader } from "@/components/layout/MenueHeader";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { VeranstaltungTable } from "./VeranstaltungTable";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";
import { getVeranstaltungenPage } from "@/api/services/veranstaltungApi";

interface State {
  data: VeranstaltungList[];
  total: number;

  page: number;
  pageSize: number;

  selectedId: number | null;

  loading: boolean;
  error: string | null;
}

class Veranstaltungen extends Component<Record<string, never>, State> {
  state: State = {
    data: [],
    total: 0,
    page: 0,
    pageSize: 20,
    selectedId: null,
    loading: true,
    error: null,
  };

  componentDidMount() {
    this.fetchData();
  }

  fetchData = async () => {
    const { page, pageSize } = this.state;

    try {
      const res = await getVeranstaltungenPage(page, pageSize);
      this.setState({
        data: res.content,
        total: res.totalElements,
        loading: false,
        error: null,
      });
    } catch {
      this.setState({
        loading: false,
        error: "Fehler beim Laden der Veranstaltungen",
      });
    }
  };

  handlePageChange = (page: number) => {
    this.setState({ page, loading: true }, this.fetchData);
  };

  handlePageSizeChange = (pageSize: number) => {
    this.setState({ pageSize, page: 0, loading: true }, this.fetchData);
  };

  render() {
    const { data, total, loading, error, selectedId } = this.state;

    return (
      <>
        <MenueHeader headerText={`${total} Veranstaltungen`} />

        {renderLoadingOrError({ loading, error })}

        <VeranstaltungTable
          data={data}
          total={total}
          page={this.state.page}
          pageSize={this.state.pageSize}
          selectedId={selectedId}
          onSelect={(row) => this.setState({ selectedId: row?.id ?? null })}
          onPageChange={this.handlePageChange}
          onPageSizeChange={this.handlePageSizeChange}
        />
      </>
    );
  }
}

export default Veranstaltungen;
