type Status = 'wip' | 'ready' | 'in progress' | 'finished';

interface Ctf {
	owner: string;
	title: string;
	description: string;
	localisation: string;
	status: Status;
	startedAt: Date | null;
	endedAt: Date | null;
}

export type { Ctf };
