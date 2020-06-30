// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    Collection<TimeRange> result = new ArrayList<>();
    for (String attendee: request.getAttendees()) {
      Collection<TimeRange> meetingTimes = getMeetingTimes(events, attendee);
      merge(result, meetingTimes);
    }

    return result;
  }

  private void merge(Collection<TimeRange> result, Collection<TimeRange> other) {
    // If result is empty, add other to the result directly
    // If result is not empty, need to merge the two, keep the overlapping ranges
    

  }

  private Collection<TimeRange> getMeetingTimes(Collection<Event> events, String attendee) {
    // Get attendee's unavailable time ranges
    // Assume required meetings for one person do not overlap
    Collection<TimeRange> meetingTimes = new ArrayList<>();
    for (Event e: events) {
      if (e.getAttendees().contains(attendee)) {
        meetingTimes.add(e.getWhen());
      }
    }
    // Collection<TimeRange> availability = new ArrayList<>();
    // int point = TimeRange.START_OF_DAY;
    // for (TimeRange t: meetingTime) {
    //   availability.add(TimeRange.fromStartEnd(point, t.start(), false));
    //   point = t.end();
    // }
    // availability.add(TimeRange.fromStartEnd(point, TimeRange.END_OF_DAY));

    return meetingTimes;
  }
}