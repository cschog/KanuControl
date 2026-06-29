// src/hooks/useUnterkunftsarten.ts

import { useEffect, useState } from "react";

import { UnterkunftsartRef } from "@/api/types/unterkunft/UnterkunftsartRef";

import { getUnterkunftsartRefs } from "@/api/services/unterkunftsartApi";

export function useUnterkunftsarten() {

    const [options, setOptions] =
        useState<UnterkunftsartRef[]>([]);

    const [loading, setLoading] =
        useState(false);

    useEffect(() => {

        const load = async () => {

            try {

                setLoading(true);

                setOptions(await getUnterkunftsartRefs());

            } finally {

                setLoading(false);

            }
        };

        load();

    }, []);

    return {
        options,
        loading,
    };
}