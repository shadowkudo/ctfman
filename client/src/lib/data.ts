import { PUBLIC_BACKEND_URL } from '$env/static/public';
import { useError } from '$lib/utils';
export type PageFetch = typeof fetch;
export type PageFetchReturn = ReturnType<PageFetch>;

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

export type { Status, Ctf };

async function fetchAllCtfs(fetch: PageFetch): Promise<Ctf[]> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/ctfs`, { credentials: 'include' });

	if (res.status == 401) {
		useError(401);
	}

	if (res.status != 200) {
		console.error(`$lib/data.ts@fetchCtfs: unexpected status: ${res.status}`);
		return [];
	}

	const json = await res.json();

	if (!Array.isArray(json)) {
		console.error(`$lib/data.ts@fetchCtfs: unexpected json body: ${json}`);
		return [];
	}

	return json.map((it) => ({
		owner: it.owner,
		title: it.title,
		description: it.description,
		localisation: it.localisation,
		status: it.status,
		startedAt: it.startedAt ? new Date(it.startedAt) : null,
		endedAt: it.endedAt ? new Date(it.endedAt) : null
	}));
}

export { fetchAllCtfs };
