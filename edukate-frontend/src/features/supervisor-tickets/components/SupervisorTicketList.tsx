import { FC, useState } from "react";
import { SupervisorTicketDto } from "@/features/supervisor-tickets/types";
import { useSupervisorTicketsQuery } from "@/features/supervisor-tickets/api";
import { SupervisorTicketsTable } from "./SupervisorTicketsTable";
import { SupervisorTicketDrawer } from "./SupervisorTicketDrawer";

const PAGE_SIZE = 20;

type SupervisorTicketListProps = {
    problemSetShareCode: string;
};

export const SupervisorTicketList: FC<SupervisorTicketListProps> = ({ problemSetShareCode }) => {
    const [page, setPage] = useState(0);
    const [selectedTicket, setSelectedTicket] = useState<SupervisorTicketDto | null>(null);

    const { data, isLoading, error } = useSupervisorTicketsQuery(problemSetShareCode, page, PAGE_SIZE);

    return (
        <>
            <SupervisorTicketsTable
                tickets={data?.content}
                totalElements={data?.totalElements ?? 0}
                isLoading={isLoading}
                error={error}
                page={page}
                size={PAGE_SIZE}
                onPageChange={setPage}
                onRowClick={setSelectedTicket}
            />
            <SupervisorTicketDrawer
                ticket={selectedTicket}
                problemSetShareCode={problemSetShareCode}
                page={page}
                size={PAGE_SIZE}
                onClose={() => {
                    setSelectedTicket(null);
                }}
            />
        </>
    );
};
