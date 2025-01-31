import type { LayoutLoad } from './$types';
import type { PageFetch } from '$lib/data';
import type { User } from '.';
import { PUBLIC_BACKEND_URL } from '$env/static/public';

export const ssr = false;

export const load: LayoutLoad = async ({ fetch }) => {
	return {
		user: await fetchUser(fetch)
	};
};

async function fetchUser(fetch: PageFetch): Promise<User | undefined> {
	let res = await fetch(`${PUBLIC_BACKEND_URL}/profile`, { credentials: 'include' });

	// User isn't logged in
	if (res.status == 401) {
		return undefined;
	}

	if (res.status != 200) {
		console.error(`Unexpected status code when fetching profile: ${res.status}`);
		return undefined;
	}

	const json = await res.json();

	if (json?.authentication == null) {
		console.error(`User missing authentication field in response: ${json}`);
		return undefined;
	}

	return {
		name: json.authentication,
		email: json?.primaryContact,
		role: {
			challenger: json?.isChallenger,
			admin: json?.isAdmin,
			moderator: json?.isModerator,
			author: json?.isAuthor
		},
		createdAt: json.createdAt ? new Date(json.createdAt) : undefined,
		deletedAt: json.deletedAt ? new Date(json.deletedAt) : undefined
	};
}
