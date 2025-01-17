import type { LayoutLoad } from './$types';
import type { PageFetch } from '$lib/data';
import type { Ctf } from '..';
import { PUBLIC_BACKEND_URL } from '$env/static/public';
import { useError } from '$lib/utils';

export const load: LayoutLoad = async ({ fetch, params }) => {
	if (!params.ctf || !params.ctf.length) {
		useError(404);
	}

	let ctf: Ctf | null = await fetchCtf(fetch, params.ctf);

	if (!ctf) {
		useError(404);
	}

	return {
		ctf
	};
};

async function fetchCtf(fetch: PageFetch, title: string): Promise<Ctf | null> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/ctfs/${title}`, { credentials: 'include' });

	if (res.status != 200) {
		console.error(`teams/+page.tx@fetchUser: unexpected status: ${res.status}`);
		return null;
	}

	const json = await res.json();

	return {
		owner: json.owner,
		title: json.title,
		description: json.description,
		localisation: json.localisation,
		status: json.status,
		startedAt: json.startedAt ? new Date(json.startedAt) : null,
		endedAt: json.endedAt ? new Date(json.endedAt) : null
	};
}
