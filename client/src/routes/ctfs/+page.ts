import type { PageLoad } from './$types';
import type { PageFetch } from '$lib/data';
import { PUBLIC_BACKEND_URL } from '$env/static/public';
import { useError } from '$lib/utils';

import type { Ctf } from '.';

export const load: PageLoad = async ({ fetch }) => {
	return {
		ctfs: await fetchCtfs(fetch)
	};
};

async function fetchCtfs(fetch: PageFetch): Promise<Ctf[]> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/ctfs`, { credentials: 'include' });

	if (res.status == 401) {
		useError(401);
	}

	if (res.status != 200) {
		console.error(`ctfs/+page.ts@fetchCtfs: unexpected status: ${res.status}`);
		return [];
	}

	const json = await res.json();

	if (!Array.isArray(json)) {
		console.error(`ctfs/+page.ts@fetchCtfs: unexpected json body: ${json}`);
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
